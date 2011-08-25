(ns vote.test.condorcet
  (:use [vote.condorcet])
  (:use [clojure.test]))

(deftest test-ranked-pairs-winner
  (is (= "c" (tab-ranked-pairs '("c" "b" "a")
    (concat
      (take 3 (repeat { "a" 1 "b" 2 "c" 3}))
      (take 2 (repeat { "b" 1 "c" 2 "a" 3}))
      (take 4 (repeat { "c" 1 "a" 2 "b" 3}))))))
  (is (= "a" (tab-ranked-pairs '("a" "b" "c")
    (concat
      (take 5 (repeat { "a" 1 "b" 2 "c" 3}))
      (take 2 (repeat { "b" 1 "c" 2 "a" 3}))
      (take 4 (repeat { "c" 1 "a" 2 "b" 3}))))))
  (is (= "c" (tab-ranked-pairs '("a" "b" "c")
    (concat
      (take 1 (repeat { "a" 1 "b" 2 "c" 3}))
      (take 1 (repeat { "b" 1 "c" 2 "a" 3}))
      (take 1 (repeat { "b" 1 "c" 1 "a" 3}))
      (take 2 (repeat { "c" 1 "a" 2 "b" 3})))))))

(deftest test-tab-ranked-pairs
  (is (= "a" (tab-ranked-pairs '("a" "c" "b") '({ "a" 1 "b" 2 "c" 3}))))
  (is (= "b" (tab-ranked-pairs '("a" "c" "b") '({ "a" 4 "b" 2 "c" 3}))))
  (is (= "a" (tab-ranked-pairs '("a" "c" "b") (take 3 (repeat { "a" 1 "b" 2 "c" 3}))))))

(deftest test-tab-ranked-pairs-matrix
  (is (= '((("a" "c") 1) (("a" "b") 1) (("b" "c") 1))
    (tab-ranked-pairs-matrix '("a" "c" "b") '({ "a" 1 "b" 2 "c" 3}))))
  (is (= '((("a" "c") 2) (("b" "c") 2) (("a" "b") 1))
    (tab-ranked-pairs-matrix '("a" "c" "b") '({ "a" 1 "b" 2 "c" 3} { "a" 1 "b" 1 "c" 3})))))

(deftest test-ranked-pairs-winner
  (is (= "a" (ranked-pairs-winner '((("a" "c") 1) (("a" "b") 1) (("b" "c") 1)))))
  (is (= "a" (ranked-pairs-winner '((("a" "b") 2) (("b" "c") 3) (("a" "c") 1))))))
