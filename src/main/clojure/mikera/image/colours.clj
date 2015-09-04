(ns mikera.image.colours
  "Namespace for colour handling and conversion functions.

   Note there are 4 different ways to specify colours:
   1. long colour values (entire colour expressed as a single long) - e.g. 0xFF00FF00
   2. long component values (individual alpha, red, green, blue) - e.g. [255 234 0 101]
   3. double colour values (individual alpha, red, green, blue) - e.g. [0.5 0.5 0.5]
   4. Java Color instances - e.g. java.awt.Color/BLACK

   Care should be taken to use the appropriate function. Typically:
   1. Offers the best performance
   2. Useful for manipulating 1.
   3. Useful for high precision computations, or colours outside normal ranges
   4. Useful for Java interop"
  (:use [mikera.cljutils error])
  (:import [mikera.image Colours])
  (:import [mikera.util Rand])
  (:import [java.awt Color]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defmacro long-colour
  "Convenience macro to cast an integer ARGB value to a long colour value."
  ([x]
  `(bit-and 0xFFFFFFFF ~x)))

;; A cache of boxed Double values
(def ^{:static true
       :tag "[Ljava.lang.Double;"} 
     DOUBLE-CACHE 
  (into-array Double (map (fn [x] (/ x 255.0)) (range 256))))

(defmacro boxed-double-value
  "Convenient macro to convert a long component value to a boxed java.lang.Double. Uses a cache of Double values."
  ([x]
    `(aget DOUBLE-CACHE (long ~x))))

(defn rgb
  "Get the long ARGB colour value specified by the RGB colour values (expresed in range 0.0-1.0).

   Unless specified the Alpha value of the resulting colour will be 1.0 (fully opaque)"
  (^long [^Color colour]
    (bit-or 0xFF000000 (long-colour (.getRGB colour))))
  (^long [r g b]
    (long-colour (Colours/getRGBClamped (double r) (double g) (double b))))
  (^long [r g b a]
    (long-colour (Colours/getARGBClamped (double a) (double r) (double g) (double b)))))

(defn argb
  "Get the long ARGB colour value specified by the ARGB colour values (expresed in range 0.0-1.0)."
  (^long [^Color colour]
    (long-colour (.getRGB colour)))
  (^long [r g b]
    (long-colour (Colours/getRGBClamped (double r) (double g) (double b))))
  (^long [r g b a]
    (long-colour (Colours/getARGBClamped (double a)  (double r) (double g) (double b)))))

(defmacro extract-alpha
  "Extracts the long alpha component (range 0-255) from a long colour"
  ([argb]
    `(bit-shift-right (bit-and (long ~argb) 0xFF000000) 24)))

(defmacro extract-red
  "Extracts the long red component (range 0-255) from a long colour"
  ([argb]
    `(bit-shift-right (bit-and (long ~argb) 0x00FF0000) 16)))

(defmacro extract-green
  "Extracts the long green component (range 0-255) from a long colour"
  ([argb]
    `(bit-shift-right (bit-and (long ~argb) 0x0000FF00) 8)))

(defmacro extract-blue
  "Extracts the long blue component (range 0-255) from a long colour"
  ([argb]
    `(bit-and (long ~argb) 0x000000FF)))

(defn components-argb
  "Return the ARGB components of a long colour value, in a 4-element vector of long component values (range 0-255)"
  ([^long argb]
   [(extract-alpha argb)
    (extract-red argb)
    (extract-green argb)
    (extract-blue argb)]))

(defn components-rgb
  "Return the RGB components of a long colour value, in a 3-element vector of long component values (range 0-255)"
  ([^long rgb]
   [(extract-red rgb)
    (extract-green rgb)
    (extract-blue rgb)]))

(defn values-rgb
  "Return the RGB components of a long colour value, in a 3-element vector of double values (range 0.0-1.0)"
  ([^long rgb]
   [(boxed-double-value (extract-red rgb))
    (boxed-double-value (extract-green rgb))
    (boxed-double-value (extract-blue rgb))]))

(defn values-argb
  "Return the ARGB components of a long colour value, in a 4-element vector of long values (range 0.0-1.0)"
  ([^long argb]
   [(boxed-double-value (extract-alpha argb))
    (boxed-double-value (extract-red argb))
    (boxed-double-value (extract-green argb))
    (boxed-double-value (extract-blue argb))]))



(defn rand-colour
  "Returns a random RGB colour value with 100% alpha"
  (^long []
    (bit-or 0xFF000000 (Rand/r 0x1000000))))

(defn rand-grayscale
  "Returns a random grayscale colour value with 100% alpha"
  (^long []
    (bit-or 0xFF000000 (* 0x10101 (Rand/r 0x100)))))

(defn color
  (^Color [rgba]
    (let [rgba (int rgba)]
      (Color. rgba true))))

(def JAVA-COLOURS
  '(black blue cyan darkGray gray green lightGray magenta
    orange pink red white yellow))

(doseq [colour JAVA-COLOURS]
  (eval `(def ~(vary-meta colour assoc :const true) (long-colour (.getRGB (. Color ~colour))))))
