(ns matross.mapstache
  (:require [potemkin :refer [def-map-type]])
  (:import clojure.lang.Atom
           clojure.lang.Associative
           clojure.lang.IFn
           clojure.lang.ILookup
           clojure.lang.IPersistentMap
           clojure.lang.Seqable
           clojure.lang.MapEntry
           clojure.lang.IPersistentCollection
           clojure.lang.IPersistentVector
           clojure.lang.SeqIterator))

(declare mapstache)

(defprotocol IRender
  (render [this template data])
  (can-render? [this v]))

(defn string-renderer [f]
  (reify IRender
    (can-render? [_ v] (string? v))
    (render [_ template data] (f template data))))

(defn no-template
  "Mark a Var as 'do not template', preventing mapstache from templating any values within it."
  [m]
  (vary-meta m assoc :mapstache-no-template true))

(defn- no-template? [m] (:mapstache-no-template (meta m)))

(defn- circular-path-message [p]
  (str "Circular key lookup: "
       (->> p
            (map (fn [path] (map name path)))
            (map (fn [path] (clojure.string/join "." path)))
            (clojure.string/join " -> "))))


(def-map-type Mapstache [^matross.mapstache.IRender renderer
                         ^IPersistentMap value
                         ^IPersistentVector cursor
                         ^Atom lookups
                         root]

  (get [this k not-found]
       (let [lookup-key (conj cursor k)
             v (get-in value lookup-key not-found)
             root (or root this)]

         (cond
           (no-template? v)
           v

           (can-render? renderer v)
           (if (= (.indexOf @lookups lookup-key) -1)
             (try
               (swap! lookups conj lookup-key)
               (render renderer v root)
               (finally (swap! lookups pop)))
             (let [message (circular-path-message (conj @lookups lookup-key))]
               (throw (IllegalArgumentException. message))))

           (instance? IPersistentMap v)
           (mapstache renderer value lookup-key lookups root)

           (sequential? v)
           (let [gen-render (fn gen* [cursor]
                              (fn [idx x]
                                (cond
                                 (can-render? renderer x) (render renderer x root)
                                 (instance? IPersistentMap x) (mapstache renderer value (conj cursor idx) lookups root)
                                 (sequential? x) (map-indexed (gen* (conj cursor idx)) x)
                                 :else x)))]
             (map-indexed (gen-render lookup-key) v))

           :else v)))

  (assoc [_ k v]
         (mapstache renderer (assoc-in value (conj cursor k) v) cursor lookups root))

  (dissoc [_ k]
          (let [new-value (if (empty? cursor)
                            (dissoc value k)
                            (update-in value cursor dissoc k))]
            (mapstache renderer new-value cursor lookups root)))

  (keys [_] (keys (get-in value cursor))))

(defn mapstache
  ([renderer value]
     (mapstache renderer value [] (atom []) nil))
  ([renderer value cursor lookups root]
     (Mapstache. renderer value cursor lookups root)))
