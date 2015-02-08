(ns mikera.image.test-pixels
  (:require [mikera.image.core :refer :all]
            [clojure.test :refer :all])
  (:import [java.awt Color]))


(deftest can-set-and-retrieve-color
  (let [img (new-image 10 10)]
    (set-pixel img 0 0 0)
    (is (= 0 (get-pixel img 0 0)))

    (set-pixel img 0 0 (.getRGB Color/WHITE))
    (is (= (.getRGB Color/WHITE) (get-pixel img 0 0)))))
