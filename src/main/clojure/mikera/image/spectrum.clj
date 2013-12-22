(ns mikera.image.spectrum
  (:use [mikera.image.colours])
  (:import [mikera.util Maths])
  (:import [java.awt.image BufferedImage]))

(defn heatmap
  (^long [^double x]
    (let [x (* 8 (- x 0.5))
          r (Maths/logistic (+ x 3.0))
          g (Maths/logistic (- x 0.0))
          b (+ (Maths/logistic (- 0.0 x )) (Maths/logistic (- x 4.0)))]
      (rgb r g b))))

(defn mono
  (^long [^double x]
    (let [x (* 6 (- x 0.5))
          r (Maths/logistic x)
          g r
          b r]
      (rgb r g b))))
