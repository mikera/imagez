(ns mikera.image.spectrum
  (:use [mikera.image.colours])
  (:import [mikera.util Maths]))

(defn heatmap
  (^long [^double x]
    (let [r (Maths/logistic (- x 1.0))
          g (Maths/logistic (+ x 0.0))
          b (Maths/logistic (+ x 1.0))])))