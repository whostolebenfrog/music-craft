(ns music-craft.core
  (:require [clojure.java.io :as io]
            [clojure.math.combinatorics :as combo]
            [play-clj
             [core :refer :all]
             [ui :refer :all]
             [g2d :as g2d]
             [g3d :refer :all]
             [math :refer :all]
             [repl :refer :all]
             [utils :as u]])
  (:import [com.badlogic.gdx.graphics.g3d.environment DirectionalLight]
           [com.badlogic.gdx.input RemoteInput]
           [com.badlogic.gdx.graphics.g3d.attributes TextureAttribute]
           [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.files FileHandle]))

;; required for the refresh function
(declare music-craft main-screen)

;; constants
(def b-size "block size" 2)
(def grid-size 20)

;; textures
(defn raw-texture
  "Creates a libgdx texture from the supplied resourece path"
  [path]
  (-> path io/resource .toURI java.io.File. FileHandle. Texture.))

(def grass-texture (delay (raw-texture "grass.jpg")))
(def stone-texture (delay (raw-texture "stone.jpg")))
(def sand-texture  (delay (raw-texture "sand.jpg")))
(def water-texture (delay (raw-texture "water.jpg")))

(defn directional-light
  "Returns a new directional light, doesn't seem to exist in play-clj"
  []
  (doto (DirectionalLight.) (.set 0.8 0.8 0.8 -1 -0.8 -0.2)))

(defn random-texture
  "Returns a random texture"
  []
  (rand-nth [grass-texture stone-texture sand-texture water-texture]))

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
  "Creats a 1 deep plane of blocks from +20 to -20 on the x and z axis"
  []
  (vec (for [x (range (- grid-size) grid-size 2)
             z (range (- grid-size) grid-size 2)]
         (block x 0 z))))

(defn translate-camera
  "Translate the camera on the x, y and z axis"
  [screen x y z]
  (doto screen
    (perspective! :translate x y z)
    (perspective! :update)))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen
             :renderer (model-batch)
             :attributes (let [ambient-attr-type (attribute-type :color :ambient-light)
                               ambient-attr (attribute :color ambient-attr-type 0.4 0.4 0.4 1)]
                           (environment :set ambient-attr :add (directional-light)))
             :camera (doto (perspective 67 (game :width) (game :height))
                       (position! 0 40 40)
                       (direction! 0 1 0)
                       (near! 0.1)
                       (far! 300)))
    (blocks))

  ;; Here we are handling some basic movement commands, ideally we would be using the mouse
  ;; here but currently I'm struggling to see how to add mouse cursor locking into
  ;; play-clj. I'll get back to this.
  :on-key-down
  (fn [{:keys [key] :as screen} entities]
    (condp = key
       (key-code :w)
       (translate-camera screen 0 0 -1)

       (key-code :s)
       (translate-camera screen 0 0 1)

       (key-code :a)
       (translate-camera screen -1 0 0)

       (key-code :d)
       (translate-camera screen 1 0 0)

       (key-code :j)
       (translate-camera screen 0 -1 0)

       (key-code :k)
       (translate-camera screen 0 1 0)

       (key-code :r)
       (on-gl (set-screen! music-craft main-screen))

       nil)
    entities)

  :on-render
  (fn [screen entities]
    (clear! 1 1 1 1)
    (render! screen entities)))

(defgame music-craft
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
