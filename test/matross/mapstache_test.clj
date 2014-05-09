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
  (testing "I can query it with a keyword."
    (let [[m ms] (matching-maps {:a "value"})]
      (is (= (:a ms) (:a m)))
      (is (= (:not-a ms) (:not-a m)))))

  (testing "I can call it like a function"
    (let [[m ms] (matching-maps {:a "value"})]
      (is (= (ms :a) (m :a)))
      (is (= (ms :not-a) (m :not-a)))))

  (testing "I can call map over it"
    (let [[m ms] (matching-maps {:a "value" :b "other-value"})]
      (is (= (map identity ms) (map identity m)))))

  (testing "I can compare a Mapstache to things"
    (let [[m ms] (matching-maps {:a "value"})]
      (is (= ms ms))
      (is (= ms m))
      (is (not (= m ms)) "I can't make map know how to check a Mapstache")
      (is (not (= ms "not a map")))))

  (testing "I can conj elements onto it"
    (let [[m ms] (matching-maps {:a "value"})
          v [:b "other-value"]]
      (is (= (conj ms v) (conj m v)))))

  (testing "I can assoc elements into it"
    (let [[m ms] (matching-maps {:a "value"})
          k :key
          v :value]
      (is (= (assoc ms k v) (assoc m k v)))))

  (testing "I can dissoc elements into it"
    (let [[m ms] (matching-maps {:a "value" :b "other-value"})
          k :b]
      (is (= (dissoc ms k) (dissoc m k)))))

  (testing "reduce-kv works as expected"
    (let [[m ms] (matching-maps {:a "value" :b "more"})
          reducer (fn [pr k v] (assoc pr v k))]
      (is (= (reduce-kv reducer {}  ms)
             (reduce-kv reducer {}  m))))))

(deftest mapstache-behavior
  (testing "sub-maps are returned as Mapstache isntances"
    (let [[m ms] (matching-maps {:a {:b "value"}})]
      (is (instance? Mapstache (:a ms)))
      (is (= (:a ms) (:a m)))))

  (testing "Strings get templated"
    (let [ms (mapstache (reify IRender (render [_ s d] 42)) {:a "{{str}}"})]
      (is (= (:a ms) 42))))

  (testing "A real template example"
      (let [ms (mustached {:a "{{str}}" :str "value"})]
        (is (= (:a ms) "value"))))

  (testing "Sub map templating works"
    (let [ms (mustached {:a "{{b.str}}" :b {:str "value"}})]
        (is (= (:a ms) "value"))))

  (testing "Recursive key lookups throws an exception"
    (let [ms (mustached {:a "{{b.c}}" :b {:c "{{a}}"}})]
        (is (thrown? IllegalArgumentException (:a ms) "value"))))

  (testing "vectors get templated"
    (let [ms (mustached {:x "value" :y ["{{x}}" "value"]})]
      (is (= (:y ms) ["value" "value"])))))
