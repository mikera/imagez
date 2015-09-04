(ns mikera.image.test-colours
  (:use mikera.image.colours)
  (:use clojure.test))

(deftest test-rgb
  (is (== 0xFF000000 (rgb 0 0 0))))

(deftest test-components-argb
  (is (= [0xAD 0xBE 0xEF 0xDE] (components-argb 0xDEADBEEF)))
  (is (= [0xAD 0xBE 0xEF 0xDE] (with-components [[r g b a] 0xDEADBEEF] [r g b a]))))

(deftest test-components-rgb
  (is (= [0xFE 0xBA 0xBE] (components-rgb 0xCAFEBABE))))

(deftest test-rand-colour
  (let [rc (rand-colour)]
    (is (== 0xFF000000 (bit-and 0xFF000000 rc)))))

(deftest test-double-cache
  (is (identical? (boxed-double-value 100) (boxed-double-value 100))))

(deftest test-color
  (is (instance? java.awt.Color (color 0xFFFFFFFF))))
