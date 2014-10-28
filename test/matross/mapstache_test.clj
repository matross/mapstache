(ns matross.mapstache-test
  (:require [clojure.test :refer :all]
            [matross.mapstache :refer :all]
            [stencil.core :as mustache])
  (:import matross.mapstache.Mapstache
           clojure.lang.MapEntry))

(defn matching-maps [m]
  [m (mapstache (string-renderer (fn [t d] t)) m)])

(defn mustached [m]
  (mapstache (string-renderer (fn [t d] (mustache/render-string t d))) m))

(deftest behaves-like-a-map
  (testing "seq behaves properly"
    (let [[m ms] (matching-maps {})]
      (is (= (seq ms) (seq m)))))

  (testing "Calling a keyword function on a Mapstache works."
    (let [[m ms] (matching-maps {:a "value"})]
      (is (= (:a ms) (:a m)))
      (is (= (:not-a ms) (:not-a m)))))

  (testing "Using a Mapstache as a function works."
    (let [[m ms] (matching-maps {:a "value"})]
      (is (= (ms :a) (m :a)))
      (is (= (ms :not-a) (m :not-a)))))

  (testing "`map` can be called on a Mapstache."
    (let [[m ms] (matching-maps {:a "value" :b "other-value"})]
      (is (= (map identity ms) (map identity m)))))

  (testing "I can compare a Mapstache to other values, but not the opposite."
    (let [[m ms] (matching-maps {:a "value"})]
      (is (= ms ms))
      (is (= ms m))
      (is (not= ms "not a map"))))

  (testing "I can conj an element onto a Mapstache."
    (let [[m ms] (matching-maps {:a "value"})
          v [:b "other-value"]]
      (is (= (conj ms v) (conj m v))))

    (let [[m ms] (matching-maps {:a {:b "value"}})
          v [:c "other-value"]]
      (is (= (conj (:a ms) v) (conj (:a  m) v)))))

  (testing "I can assoc key/values into a Mapstache."
    (let [[m ms] (matching-maps {:a "value"})
          k :key
          v :value]
      (is (= (assoc ms k v) (assoc m k v)))))

  (testing "I can dissoc keys from a Mapstache."
    (let [[m ms] (matching-maps {:a "value" :b "other-value"})
          k :b]
      (is (= (dissoc ms k) (dissoc m k)))))

  (testing "I can call reduce-kv on a Mapstache."
    (let [[m ms] (matching-maps {:a "value" :b "more"})
          reducer (fn [pr k v] (assoc pr v k))]
      (is (= (reduce-kv reducer {}  ms)
             (reduce-kv reducer {}  m)))))

  (testing "I can check if a Mapstache contains a key."
    (let [[m ms] (matching-maps {:a "value"})]
      (is (contains? ms :a))
      (is (not (contains? ms :b)))))

  (testing "I can call `keys` on a Mapstache."
    (let [[m ms] (matching-maps {:a "value"})]
      (is (= (keys ms) (keys m)))))

  (testing "I can call `vals` on a Mapstache"
    (let [[m ms] (matching-maps {:a "value"})]
      (is (= (vals ms) (vals m)))))

  (testing "I can call `find` on a Mapstache"
    (let [[m ms] (matching-maps {:a "value"})]
      (is (= (find ms :a) (find m :a)))
      (is (= (find ms :not-a) (find m :not-a)))))

  (testing "I can call `first` on a Mapstache"
    (let [[m ms] (matching-maps {:a "value"})]
      (is (first ms))
      (is (= (first ms) (first m)))))

  (testing "I can call `rest` on a Mapstache"
    (let [[m ms] (matching-maps {:a "value"})]
      (is (= (rest ms) (rest m)))))
  (testing "get-in behaves like it does on a normal map"
    (let [[m ms] (matching-maps {:a '(1 2 3)})]
      (is (= (get-in ms [:a 1]) (get-in m [:a 1])))
      (is (= (get-in ms [:a 10]) (get-in m [:a 10]))))
    (let [[m ms] (matching-maps {:a [1 2 3]})]
      (is (= (get-in ms [:a 10]) (get-in m [:a 10])))
      (is (= (get-in ms [:a 1]) (get-in m [:a 1]))))
    (let [[m ms] (matching-maps {:a #{1 2 3}})]
      (is (= (get-in ms [:a 10]) (get-in m [:a 10])))
      (is (= (get-in ms [:a 1]) (get-in m [:a 1])))) ))

(deftest mapstache-behavior
  (testing "Querying maps wraps the value in a Mapstache"
    (let [[m ms] (matching-maps {:a {:b "value"}})]
      (is (instance? Mapstache (:a ms)))
      (is (= (:a ms) (:a m)))))

  (testing "Querying Strings renders the value as a template"
    (let [ms (mapstache (reify IRender
                          (render [_ s d] 42)
                          (can-render? [_ v] true)) {:a "{{str}}"})]
      (is (= (:a ms) 42))))

  (testing "An actual template engine behaves properly"
    (let [ms (mustached {:a "{{str}}" :str "value"})]
      (is (= (:a ms) "value"))))

  (testing "Values in a map get resolved properly"
    (let [ms (mustached {:a "{{b.str}}" :b {:str "value"}})]
      (is (= (:a ms) "value"))))

  (testing "Recursive key lookups throw an exception instead of deadlocking"
    (let [ms (mustached {:a "{{b.c}}" :b {:c "{{a}}"}})]
      (is (thrown? IllegalArgumentException (:a ms) "value"))))

  (testing "Vector elements get individually evaluated before being returned"
    (let [ms (mustached {:x "value" :y ["{{x}}" "value"]})]
      (is (= (:y ms) ["value" "value"]))))

  (testing "Deeply traverses through vectors"
    (let [ms (mustached {:x "value" :y [ {:x "{{x}}"} ["{{x}}"] ]})]
      (is (= (:y ms) [{:x "value"} ["value"]]))))

  (testing "toString can render an instance of Mapstache to a string"
    (let [ms (mustached {:x "{{y}}" :y {:a :b}})]
      (is (not (empty? (str (:x ms)))))))

  (testing "can lookup across complex objects"
    (let [ms (mustached {:x {:a "{{y.b}}"} :y {:a "{{x.a}}" :b 23}})]
      (is (= "23" (get-in ms [:y :a])))))

  (testing "can select keys"
    (let [ms (mustached {:a "{{str}}" :str "value"})]
      (is (= {:a "value"} (select-keys ms [:a])))))

  (testing "Values can be marked as 'no-template', disabling Mapstache's behavior"
    (let [[m ms] (matching-maps (no-template {:a "{{b}}" :b "c"}))]
      (is (= (:a ms) (:a m)))))
  
   (testing "can return lists properly"
    (is (= '(0 1 2) (:key {:key '(0 1 2)}))) ;; normal map behavior
    (is (= '(0 1 2) (:key (mustached {:key '(0 1 2)})))))

  (testing "can be nested multiple times"
    (is (= '(0 1 2) (:key (mustached (mustached {:key [0 1 2]}))))))) 
