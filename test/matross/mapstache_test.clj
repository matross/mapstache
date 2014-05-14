(ns matross.mapstache-test
  (:require [clojure.test :refer :all]
            [matross.mapstache :refer :all]
            [clostache.parser :as mustache])
  (:import matross.mapstache.Mapstache
           clojure.lang.MapEntry))

(defn matching-maps [m]
  [m (mapstache (reify IRender (render [_ s d] s)) m)])

(defn mustached [m]
  (mapstache
   (reify IRender
     (render [_ s d] (mustache/render s d)))
   m))

(deftest behaves-like-a-map
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
      (is (not (= m ms)) "I can't make map know how to check a Mapstache")
      (is (not (= ms "not a map")))))

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
      (is (= (find ms :not-a) (find m :not-a))))))

(deftest mapstache-behavior
  (testing "Querying maps wraps the value in a Mapstache"
    (let [[m ms] (matching-maps {:a {:b "value"}})]
      (is (instance? Mapstache (:a ms)))
      (is (= (:a ms) (:a m)))))

  (testing "Querying Strings renders the value as a template"
    (let [ms (mapstache (reify IRender (render [_ s d] 42)) {:a "{{str}}"})]
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

  (testing "toString can render an instance of Mapstache to a string"
    (let [ms (mustached {:x "{{y}}" :y {:a :b}})]
      (is (not (empty? (str (:x ms)))))))

  (testing "Values can be marked as 'no-template', disabling Mapstache's behavior"
    (let [[m ms] (matching-maps (no-template {:a "{{b}}" :b "c"}))]
      (is (= (:a ms) (:a m))))))
