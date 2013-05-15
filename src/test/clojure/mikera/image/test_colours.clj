(ns mikera.image.test-colours
  (:use mikera.image.colours)
  (:use clojure.test))

(deftest test-rgb
  (is (== 0xFF000000 (rgb 0 0 0))))

(deftest test-rand-colour
  (let [rc (rand-colour)]
    (is (== 0xFF000000 (bit-and 0xFF000000 rc)))))

