(ns mikera.image.spectrum
  "Namespce for colour spectrum functions. These functions convert a double value from 0.0 to 1.0 to a 
   long ARGB colour value."
  (:use [mikera.image.colours])
  (:import [mikera.util Maths])
  (:import [java.awt.image BufferedImage]))

;; set of spectrum generation functions
;;
;; each of these maps a double value to an ARGB colour
;;
;; colours are generally viewable in the range 0..1
;; though the spectrums can continue / be generated outside this range

(def ^:const THIRD-TAU (/ Maths/TAU 3.0))

(defn heatmap
  (^long [^double x]
    (let [x (* 8 (- x 0.5))
          r (Maths/logistic (+ x 3.0))
          g (Maths/logistic (- x 0.0))
          b (+ (Maths/logistic (- 0.0 x )) (Maths/logistic (- x 4.0)))]
      (rgb r g b))))

(defn mono
  "Compute a monochromatic colour spectrum, from 0.0=black to 1.0=white" 
  (^long [^double x]
    (let [x (* 6 (- x 0.5))
          r (Maths/logistic x)
          g r
          b r]
      (rgb r g b))))

(defn wheel
  "Compute a simple colour wheel spectrum, from 0.0 to 1.0" 
  (^long [^double x]
    (let [x (- x (java.lang.Math/floor x))
          x (* x Maths/TAU)
          t THIRD-TAU
          r (Math/cos x)
          g (Math/cos (- x t))
          b (Math/cos (+ x t))
          r (+ 0.5 (* 0.5 r))
          g (+ 0.5 (* 0.5 g))
          b (+ 0.5 (* 0.5 b))]
      (rgb r g b))))
