(ns mikera.image.colours
  (:use [mikera.cljutils error])
  (:import [mikera.image Colours])
  (:import [java.awt Color]))

(defmacro long-colour 
  "Macro to convert an integer ARGB value to a long colour value."
  ([x]
  `(bit-and 0xFFFFFFFF ~x)))

(defn rgb ^long [r g b]
  (long-colour (Colours/getRGBClamped (double r) (double g) (double b))))

(defn argb ^long [a r g b]
  (long-colour (Colours/getARGBClamped (double a)  (double r) (double g) (double b))))

(defn components-argb 
  "Return the ARGB components of a colour value, in a 4-element vector of double values"
  ([^long argb]
    (TODO)))

(defn components-rgb 
  "Return the RGB components of a colour value, in a 3-element vector of double values"
  ([^long argb]
    (TODO)))

(def JAVA-COLOURS
  '(black blue cyan darkGray gray green lightGray magenta 
    orange pink red white yellow))

(doseq [colour JAVA-COLOURS]
  (eval `(def ~colour (long-colour (.getRGB (. Color ~colour))))))