(ns mikera.image.protocols
  (:require [clojure.java.io :refer [as-file file]])
  (:import java.io.File
           java.net.URI
           java.net.URL
           javax.imageio.ImageIO
           java.awt.image.BufferedImage))

(defprotocol ImageResource
  "Coerce different image resource representations to BufferedImage."
  (as-image [x] "Coerce argument to an image."))

(extend-protocol ImageResource
  String
  (as-image [s] (ImageIO/read (as-file s)))

  File
  (as-image [f] (ImageIO/read f))

  URL
  (as-image [r] (ImageIO/read r))

  BufferedImage
  (as-image [b] b))
