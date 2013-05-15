(ns mikera.image.colours
  (:use [mikera.cljutils error])
  (:import [mikera.image Colours]))

(defn rgb ^long [r g b]
  (bit-and 0xFFFFFFFF (Colours/getRGBClamped (double r) (double g) (double b))))

(defn argb ^long [a r g b]
  (bit-and 0xFFFFFFFF (Colours/getARGBClamped (double a)  (double r) (double g) (double b))))

(defn components-argb 
  "Return the ARGB components of a colour value, in a 4-element vector of double values"
  ([^long argb]
    (TODO)))

(defn components-rgb 
  "Return the RGB components of a colour value, in a 3-element vector of double values"
  ([^long argb]
    (TODO)))