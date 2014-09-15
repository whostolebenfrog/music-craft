(ns music-craft.core
  (:require [play-clj
             [core :refer :all]
             [ui :refer :all]
             [g3d :refer :all]
             [math :refer :all]
             [repl :refer :all]
             [utils :as u]])
  (:import [com.badlogic.gdx.graphics.g3d.environment DirectionalLight]
           [com.badlogic.gdx.input RemoteInput]))

(declare music-craft main-screen)

(def remote-input (RemoteInput.))
(doto remote-input (.setCursorCatched true))
(doto remote-input (.setCursorPosition 0 0))

(defn directional-light
  "Returns a new directional light, doesn't seem to exist in play-clj"
  []
  (doto (DirectionalLight.)
    (.set 0.8 0.8 0.8 -1 -0.8 -0.2)))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen
             :renderer (model-batch)
             :attributes (let [ambient-attr-type (attribute-type :color :ambient-light)
                               ambient-attr (attribute :color ambient-attr-type 0.4 0.4 0.4 1)]
                           (environment :set ambient-attr :add (directional-light)))
             :camera (doto (perspective 67 (game :width) (game :height))
                       (position! 0 0 20)
                       (direction! 0 0 0)
                       (near! 0.1)
                       (far! 300)))
    (let [attr (attribute! :color :create-diffuse (color :green))
          model-mat (material :set attr)
          model-attrs (bit-or (usage :position) (usage :normal))
          builder (model-builder)]
      (-> (model-builder! builder :create-box 2 2 2 model-mat model-attrs)
          model
          (assoc :x 0 :y 0 :z 0))))

  :on-key-down
  (fn [{:keys [key] :as screen} entities]
    (condp = key
       (key-code :w)
       (doto screen
         (perspective! :translate 0 0 -1)
         (perspective! :update))

       (key-code :s)
       (doto screen
         (perspective! :translate 0 0 1)
         (perspective! :update))

       (key-code "r")
       (on-gl (set-screen! music-craft main-screen))

       nil)
    entities)

  :on-mouse-moved
  (fn [screen entities]
    (prn (u/get-obj screen :input-listeners))
    (prn (input->screen screen (input! :get-x) (input! :get-y)))
    entities)

  :on-render
  (fn [screen entities]
    (clear! 1 1 1 1)
    (render! screen entities)))

(defgame music-craft
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
