(ns curve-fitting.model.trace-test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [metaprob.interpreters :refer [infer]]
            [metaprob.builtin :as metaprob]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]))

(deftest test-point-subtrace
  (is (metaprob/trace? (trace/point-subtrace {})))
  (is (= 1234 (trace/point-y (trace/point-subtrace {:y 1234}))))
  (is (true? (trace/point-outlier? (trace/point-subtrace {:outlier? true}))))
  (is (= 1234 (trace/point-y (trace/point-subtrace {:y 1234, :outlier? true}))))
  (is (true? (trace/point-outlier? (trace/point-subtrace {:y 1234, :outlier? true})))))

(deftest test-point
  (testing "round trip"
    (doseq [point [{}
                   {:y 1}
                   {:outlier? false}
                   {:y 2, :outlier? true}]]
      (is (= point (trace/point (trace/point-subtrace point)))))))

(deftest test-points-trace
  (is (metaprob/trace? (trace/points-trace [])))
  (is (metaprob/trace? (trace/points-trace [{}])))
  (is (metaprob/trace? (trace/points-trace [{:y 1234, :outlier? true}])))
  (let [trace (trace/points-trace [{:y 1234, :outlier? true}])]
    (is (= 1234 (trace/point-y (first (trace/point-subtraces trace)))))
    (is (true? (trace/point-outlier? (first (trace/point-subtraces trace))))))
  (testing "round trip"
    (let [point-a {:y 1, :outlier? true}
          point-b {:y 2, :outlier? false}
          point-c {:y 3}]
      (= [point-a] (trace/points (trace/points-trace [point-a])))
      (= [point-a point-b point-c] (trace/points
                                    (trace/points-trace [point-a point-b point-c])))))
  (testing "infer"
    (let [points [{:x 1, :y 4, :outlier? false}
                  {:x 2, :y 5, :outlier? true}
                  {:x 3, :y 6, :outlier? false}]
          [_ trace _] (infer :procedure model/curve-model
                             :inputs [(map :x points)]
                             :target-trace (trace/points-trace points))]
      (is (= (map #(select-keys % [:y :outlier?]) points)
             (trace/points trace))))))

(deftest test-outliers
  (testing "is set"
    (let [[_ trace _] (infer :procedure model/curve-model
                             :inputs [[1 2 3]])]
      (is (seq (trace/outliers trace)))))
  (let [[_ trace _] (infer :procedure model/curve-model
                           :inputs [[1 2 3]]
                           :target-trace (trace/points-trace (repeat 3 {:outlier? true}))
                           :intervention-trace (trace/outliers-trace false))]
    (is (every? false? (trace/outliers trace)))))

(let [[_ trace _] (infer :procedure model/curve-model
                         :inputs [[1 2 3]]
                         :target-trace (trace/points-trace (repeat 3 {:outlier? true}))
                         :intervention-trace (trace/outliers-trace false))]
  (trace/outliers trace))

(deftest test-outliers-trace
  (testing "round trip"
    (is (true? (trace/outliers-enabled? (trace/outliers-trace true))))
    (is (false? (trace/outliers-enabled? (trace/outliers-trace false)))))
  (testing "infer"
    (let [outliers? true
          [_ trace _] (infer :procedure model/curve-model
                             :inputs [[1 2 3]]
                             :target-trace (metaprob/empty-trace)
                             :intervention-trace (trace/outliers-trace outliers?))]
      (is (= outliers? (trace/outliers-enabled? trace))))))
