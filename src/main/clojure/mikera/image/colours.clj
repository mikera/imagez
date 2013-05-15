(ns mikera.image.colours
  (:import [mikera.image Colours]))

(defn rgb ^long [r g b]
  (bit-and 0xFFFFFFFF (Colours/getRGBClamped (double r) (double g) (double b))))

(defn argb ^long [a r g b]
  (bit-and 0xFFFFFFFF (Colours/getARGBClamped (double a)  (double r) (double g) (double b))))
