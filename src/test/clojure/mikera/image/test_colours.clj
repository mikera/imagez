(ns mikera.image.test-colours
  (:use mikera.image.colours)
  (:use clojure.test))

(deftest test-rgb
  (is (== 0xFF000000 (rgb 0 0 0))))

(deftest test-components-argb
  (is (= [0xDE 0xAD 0xBE 0xEF] (components-argb 0xDEADBEEF))))

(deftest test-components-rgb
  (is (= [0xFE 0xBA 0xBE] (components-rgb 0xCAFEBABE))))

(deftest test-rand-colour
  (let [rc (rand-colour)]
    (is (== 0xFF000000 (bit-and 0xFF000000 rc)))))
