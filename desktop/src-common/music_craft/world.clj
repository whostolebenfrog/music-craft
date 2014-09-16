(ns music-craft.world
  (:require [clojure.java.io :as io]
            [play-clj
             [core :refer :all]
             [ui :refer :all]
             [g2d :as g2d]
             [g3d :refer :all]
             [math :refer :all]
             [repl :refer :all]
             [utils :as u]])
  (:import [clisk.noise Simplex]
           [com.badlogic.gdx.files FileHandle]
           [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g3d.attributes TextureAttribute]))

(defn noise-for-grid
  "Returns a grid of noise values between min and max for grid x-size and y-size"
  [x-size y-size min max seed]
  (Simplex/seed seed)
  (let [output (make-array Integer/TYPE x-size y-size)]
    (doseq [x (range 0 x-size)
            y (range 0 y-size)]
      (let [nor-x (/ x x-size)
            nor-y (/ y y-size)
            noise (Simplex/noise nor-x nor-y)
            r-height (Math/round (* noise (- max min)))
            min-height (+ min r-height)]
        (aset output x y (Integer. min-height))))
    output))

;; constants
(def b-size "block size" 1)
(def grid-size 50)

;; textures
(defn raw-texture
  "Creates a libgdx texture from the supplied resourece path"
  [path]
  (-> path io/resource .toURI java.io.File. FileHandle. Texture.))

(def grass-texture (delay (raw-texture "grass.jpg")))
(def stone-texture (delay (raw-texture "stone.jpg")))
(def sand-texture  (delay (raw-texture "sand.jpg")))
(def water-texture (delay (raw-texture "water.jpg")))

(defn random-texture
  "Returns a random texture"
  []
  (rand-nth [grass-texture sand-texture sand-texture sand-texture water-texture]))

(defn block
  "Creates a block at pos x, y, z with a random texture"
  [x y z]
  (let [texture-attr (TextureAttribute. TextureAttribute/Diffuse @(random-texture))
        model-mat (material :set texture-attr)
        model-attrs (bit-or (usage :position) (usage :normal) (usage :texture-coordinates))
        builder (model-builder)]
    (-> (model-builder! builder :create-box b-size b-size b-size model-mat model-attrs)
        model
        (assoc :x x :y y :z z))))

(defn blocks
  "Create the block entities"
  []
  (let [seed 1337
        min 1
        max 10
        noise (noise-for-grid grid-size grid-size min max seed)]
    (vec (for [x (range grid-size)
               z (range grid-size)
               y (range min (aget noise x z))]
           (block x y z)))))
