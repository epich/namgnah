(ns hangman.test.StrategyTest
  (:require clojure.test)
  (:use [hangman.StrategyImpl])
  (:use [clojure.test])
  (:gen-class
    :methods [#^{:static true} [runTest [] void]]))

(defn -runTest []
  (println "Hello from Clojure run-test.")
  (run-all-tests))

;; (deftest replace-me ;; FIXME: write
;;   (is false "No tests have been written."))

;; (deftest addition
;;   (is (= 4 (+ 2 2)))
;;   (is (= 7 (+ 3 4))))

;; (deftest subtraction
;;   (is (= 1 (- 4 3)))
;;   (is (= 3 (- 666 4))))

