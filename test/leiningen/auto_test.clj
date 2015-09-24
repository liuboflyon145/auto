;;auto generate clojure test file
(ns leiningen.auto-test
  (:require [clojure.test :refer :all]
            [leiningen.auto :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (not= 0 1)))) 