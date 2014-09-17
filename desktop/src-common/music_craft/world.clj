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
(def grid-size 40)

;; textures
(defn raw-texture
  "Creates a libgdx texture from the supplied resourece path"
  [path]
  (-> path io/resource .toURI java.io.File. FileHandle. Texture.))

(defn load-material
  "Creates a new material from a texture"
  [path]
  (material :set (TextureAttribute. TextureAttribute/Diffuse (raw-texture path))))

(def grass-material (delay (load-material "grass.jpg")))
(def stone-material (delay (load-material "stone.jpg")))
(def sand-material  (delay (load-material "sand.jpg")))
(def water-material (delay (load-material "water.jpg")))

(defn random-material
  "Returns a random texture"
  []
  (rand-nth [grass-material sand-material sand-material sand-material water-material]))

(def builder (model-builder))

(defn block
  "Creates a block at pos x, y, z with a random texture"
  [x y z]
  (let [model-attrs (bit-or (usage :position) (usage :normal) (usage :texture-coordinates))]
    (-> (model-builder! builder :create-box b-size b-size b-size @(random-material) model-attrs)
        model
        (assoc :x x :y y :z z))))

(defn blocks
  "Create the block entities"
  []
  (let [seed 123234
        min 1
        max 8
        noise (noise-for-grid grid-size grid-size min max seed)]
    (vec (for [x (range grid-size)
               z (range grid-size)
               y (range min (aget noise x z))]
           (block x y z)))))
