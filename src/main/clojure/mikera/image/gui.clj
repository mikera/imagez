(ns mikera.image.gui
  "Namespace for showing images in a window"
  (:require [mikera.image.core :as core])
  (:import [java.awt.image BufferedImage])
  (:import [mikera.gui Frames]))

(defn show
  "Displays an image in a new frame.

   The frame includes simple menus for saving an image, and other handy utilities.

   Options can be supplied in keyword arguments as follows:
     :zoom   - zoom the image by a specified factor, e.g. 2.0. Performs smoothing
     :resize - resizes the image by either a specified factor or to a given target shape e.g. [256 256]
     :title  - specifies the title of the resulting frame
   "
  ([image & {:keys [zoom resize title]}]
    (let [^BufferedImage image (if zoom (core/zoom image (double zoom)) image)
          ^BufferedImage image (if resize (core/copy image resize) image)
          title (or title "Imagez Frame")]
      (Frames/display image (str title)))))
