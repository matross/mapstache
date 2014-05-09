(ns mapstache.core-test
  (:require [clojure.test :refer :all]
            [mapstache.core :refer :all])
  (:import mapstache.core.Mapstache
           clojure.lang.MapEntry))

(defn matching-maps [m]
  [m (mapstache (reify IRender (render [_ s d] s)) m)])

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
      (is (= (dissoc ms k) (dissoc m k))))))

(deftest mapstache-behavior
  (testing "sub-maps are returned as Mapstache isntances"
    (let [[m ms] (matching-maps {:a {:b "value"}})]
      (is (instance? Mapstache (:a ms)))
      (is (= (:a ms) (:a m)))))
  (testing "Strings get templated"
    (let [ms (mapstache (reify IRender (render [_ s d] 42)) {:a "{{ str }}"})]
      (is (= (:a ms) 42))))
)
