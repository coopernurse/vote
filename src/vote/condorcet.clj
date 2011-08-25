(ns vote.condorcet
  (:require [clojure.contrib.combinatorics :as combin])
  (:import (java.io BufferedReader FileReader)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; common impl
;;;;;;;;;;;;;

(defn score-matchup
  "Takes a two-element matchup and a ballot map and returns an integer
  1  - first candidate is ranked higher than second
  -1 - second candidate is ranked higher than first
  0  - candidates are ranked the same"
  [m b]
  (let [rank-a (b (first m)) rank-b (b (second m))]
    (cond
      (= rank-a rank-b) 0
      (= nil rank-b) 1
      (= nil rank-a) -1
      :default (if (< 0 (- rank-b rank-a)) 1 -1))))

(defn slice-pairs-set [pairs f]
  (set (map (fn [x] (f (first x))) pairs)))

(defn condorcet-winner-set
  [pairs]
  (clojure.set/difference (slice-pairs-set pairs first) (slice-pairs-set pairs second)))

(defn condorcet-winner
  [pairs]
  (let [winners (condorcet-winner-set pairs)]
      (if (= 1 (count winners)) (first winners) nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; v1 impl
;;;;;;;;;;;;;

(defn score-matchups-1
  "Takes a sequence of two-element matchups and a ranked ballot
  and returns a seq of each matchup and its score for the given ballot"
  [matchups ballot]
  (map #(list % (score-matchup % ballot)) matchups))

(defn sum-matchup-scores-1
  [res scores]
  (let [matchup (first (first scores))]
    (conj res (list matchup (reduce (fn [sum x] (+ sum (second x))) 0 scores)))))

(defn score-ballots-1
  [matchups ballots]
  (group-by
    (fn [x] (first x))
    (apply concat (map #(score-matchups-1 matchups %) ballots))))

(defn get-matchups-1
  [candidates]
  (combin/combinations candidates 2))

(defn reorder-pairs-winners-first-1
  [pairs]
  (map (fn [pair] (if (> 0 (second pair)) (list (reverse (first pair)) (* -1 (second pair))) pair)) pairs))

(defn ranked-pairs-winner-1
  [pairs]
  (loop [p pairs]
    (if (empty? p) nil
      (let [winner (condorcet-winner p)]
       (if winner winner (recur (drop-last p)))))))

(defn tab-ranked-pairs-matrix-1
  [candidates ballots]
  (sort-by second >
    (filter #(> (second %) 0)
      (reorder-pairs-winners-first-1
        (reduce sum-matchup-scores-1 []
          (vals (score-ballots-1 (get-matchups-1 candidates) ballots)))))))

(defn tab-ranked-pairs-1
  [candidates ballots]
  (ranked-pairs-winner-1 (tab-ranked-pairs-matrix-1 candidates ballots)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; v2 impl
;;;;;;;;;;;;;

(defn score-ballot
  [matchups ballot]
  (doall
    (map
      (fn [m] (reset! (matchups m) (+ @(matchups m) (score-matchup m ballot))))
      (keys matchups))))

(defn score-ballots
  [matchups ballots]
  (doall (map (fn [b] (score-ballot matchups b)) ballots))
  matchups)

(defn get-matchups
  [candidates]
  (apply array-map
    (interleave
      (combin/combinations candidates 2)
      (repeatedly (fn [] (atom 0))))))

(defn transform-matchups-winners-first
  [matchups]
    (map
      (fn [pair]
        (let [k (first pair) v @(second pair)]
        (if (> 0 v) (list (reverse k) (* -1 v)) (list k v))))
      matchups))

(defn ranked-pairs-winner
  [pairs]
  (loop [p pairs]
    (if (empty? p) nil
      (let [winner (condorcet-winner p)]
       (if winner winner (recur (drop-last p)))))))

(defn tab-ranked-pairs-matrix
  [candidates ballots]
  (sort-by second >
    (filter #(> (second %) 0)
      (transform-matchups-winners-first
        (score-ballots (get-matchups candidates) ballots)))))

(defn tab-ranked-pairs
  [candidates ballots]
  (ranked-pairs-winner (tab-ranked-pairs-matrix candidates ballots)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn heap-used []
  (- (.totalMemory (Runtime/getRuntime)) (.freeMemory (Runtime/getRuntime))))

(defmacro heap-delta [expr]
  `(let [tmp# (System/gc)
         before# (heap-used)
         out# ~expr]
    { :result out# :delta (- (heap-used) before#) }))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;
; pretty neat - write out a data file with a bunch of lines
;
; (clojure.contrib.duck-streams/write-lines "output.txt"
;   (concat (take 50000 (repeat { "a" 1 "b" 2 "c" 3}))
;   (take 200000 (repeat { "b" 1 "c" 2 "a" 3})) (take 40000 (repeat { "c" 1 "a" 2 "b" 3}))))

;
; run the tabulator with a given file
;
(defn tab-ranked-pairs-file [candidates file-name]
  (with-open [rdr (BufferedReader. (FileReader. file-name))]
    (tab-ranked-pairs candidates (map read-string (line-seq rdr)))))

(defn tab-ranked-pairs-file-1 [candidates file-name]
  (with-open [rdr (BufferedReader. (FileReader. file-name))]
    (tab-ranked-pairs-1 candidates (map read-string (line-seq rdr)))))

