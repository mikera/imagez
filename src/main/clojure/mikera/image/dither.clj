(ns mikera.image.dither
  (:require [mikera.image.core :as core])
  (:require [mikera.image.colours :as col])
  (:import [java.awt.image BufferedImage BufferedImageOp]))

(defn mono-palette-function 
  ([]
    (fn ^long [^double r ^double g ^double b]
      (let [grey (+ (* r 0.2989) (* g 0.5870) (* b 0.1140))]
        (if (< grey 0.5) 0xFF000000 0xFFFFFFFF)))))

(defn greyscale-palette-function 
  ([levels] 
    (fn ^long [^double r ^double g ^double b]
      (let [grey (+ (* r 0.2989) (* g 0.5870) (* b 0.1140))
            n (dec levels)
            grey (/ (Math/round (* grey n)) n)]
        (col/rgb grey grey grey)))))

(defn colour-palette-function 
  ([levels] 
    (fn ^long [^double r ^double g ^double b]
      (let [n (dec levels)
            r (/ (Math/round (* r n)) n)
            g (/ (Math/round (* g n)) n)
            b (/ (Math/round (* b n)) n)]
        (col/rgb r g b)))))

(defmacro bound-double
  [max exp]
  `(Math/max 0.0 (Math/min (double ~max) (double ~exp))))

(defn dither 
  "Dithers an image. Palette function should take an long colour value and return an long colour result."
  ([^BufferedImage image 
    palette-function]
    (let [w (long (core/width image))
          h (long (core/height image))
          er0 (double-array (inc w))
          eg0 (double-array (inc w))
          eb0 (double-array (inc w))
          er1 (double-array (inc w))
          eg1 (double-array (inc w))
          eb1 (double-array (inc w))
          ^BufferedImage result (core/new-image w h)]
      (loop [y (long 0)
             ^doubles er0 er0
             ^doubles eg0 eg0
             ^doubles eb0 eb0
             ^doubles er1 er1
             ^doubles eg1 eg1
             ^doubles eb1 eb1]
        (when (< y h)
          (dotimes [x w]
            (let [c (long (core/get-pixel image x y))
                  r (+ (aget er0 x) (col/extract-red c))
                  g (+ (aget eg0 x) (col/extract-green c))
                  b (+ (aget eb0 x) (col/extract-blue c))
                  dc (palette-function (* r 0.00392156862745098) 
                                       (* g 0.00392156862745098) 
                                       (* b 0.00392156862745098))
                  er (- r (col/extract-red dc))
                  eg (- g (col/extract-green dc))
                  eb (- b (col/extract-blue dc))]
              (core/set-pixel result x y dc)
              (let [z 0.57 i (inc x)] 
                (aset er0 i (+ (* z er) (aget er0 i)))
                (aset eg0 i (+ (* z eg) (aget eg0 i)))
                (aset eb0 i (+ (* z eb) (aget eb0 i))))
              (when (> x 0)
                (let [z 0.13 i (dec x)]
                  (aset er1 i (+ (* z er) (aget er1 i)))
                  (aset eg1 i (+ (* z eg) (aget eg1 i)))
                  (aset eb1 i (+ (* z eb) (aget eb1 i)))))
              (let [z 0.17 i x]
                (aset er1 i (+ (* z er) (aget er1 i)))
                (aset eg1 i (+ (* z eg) (aget eg1 i)))
                (aset eb1 i (+ (* z eb) (aget eb1 i))))
              (let [z 0.13 i (inc x)]
                (aset er1 i (+ (* z er) (aget er1 i)))
                (aset eg1 i (+ (* z eg) (aget eg1 i)))
                (aset eb1 i (+ (* z eb) (aget eb1 i))))))
          (dotimes [x w] (aset er0 x 0.0) (aset eg0 x 0.0) (aset eb0 x 0.0))
          (recur (inc y) er1 eg1 eb1 er0 eg0 eb0) ;; swap colour arrays
          ))
      result)))