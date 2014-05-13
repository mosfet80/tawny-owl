;; The contents of this file are subject to the LGPL License, Version 3.0.
;;
;; Copyright (C) 2013, 2014, Phillip Lord, Newcastle University
;;
;; This program is free software: you can redistribute it and/or modify it
;; under the terms of the GNU Lesser General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or (at your
;; option) any later version.
;;
;; This program is distributed in the hope that it will be useful, but WITHOUT
;; ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
;; FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
;; for more details.
;;
;; You should have received a copy of the GNU Lesser General Public License
;; along with this program. If not, see http://www.gnu.org/licenses/.
(ns
    ^{:doc "Enable querying over ontologies."
      :author "Phillip Lord"}
    tawny.query
  (:use [tawny.owl])
  (:use [tawny.render])
  (:require [tawny.util])
  (:require [clojure.core.logic :as l]))

(def
  ^{:doc "Map between a form entity and a keyword"
    :private true}
  typemap
  {'defclass :class
   'owl-class :class
   'defoproperty :oproperty
   'object-property :oproperty
   'defindividual :individual
   'individual :individual
   'defdproperty :dproperty
   'data-property :dproperty
   'defannotationproperty :aproperty
   'annotation-property :aproperty
   }
  )

(defn into-map
  "Translates an owl object into a clojure map.
The map is similar to the form used to define an entity. Keys are the keywords
used by tawny.owl (:subclass, :domain, etc). Value are sets. Each element of
the set is either an OWL object, or, if it is a restriction, similar to that
used to define a restriction but with OWLObjects instead of clojure symbols.
In addition a :type key is added which describes the type of the object."
  [owlobject]
  (let [render
        (as-form owlobject :keyword :object)]
    (apply hash-map
           (concat
            [:type (list (first render))]
            (tawny.util/groupify
             (drop 2 render))))))

(defn into-map-with
  "As into-map but merges result from other entities retrieved by (f entity).
For example (into-map-with superclasses A) will retrieve all data for A and
its superclasses. No attempt is made to ensure that the semantics of this data
makes sense; for instance, while object restrictions which apply to a
superclass also apply to the subclass, annotations do not; both will be
present in the final map, however."
  [f entity]
  (apply merge-with clojure.set/union
         (map #(dissoc (into-map %) :type)
              (filter named-object?
                      (conj (f entity)
                            entity)))))



(defn frameo [entity query
              frame]
  (l/fresh [a]
         (l/featurec
          entity
          {frame a})
         (l/everyg
          #(l/membero %1 a)
          (get query frame))))

(defn every-frameo [entity query]
  (println "bob")
  (l/everyg
   #(frameo entity query %1)
   (keys query)))

(defn noisydissoc [coll key]
  (println "coll:key" coll ":" key))

(defn matcher
  [entity query]
  (println "Here is a print statement")
  (l/all
   (every-frameo
    (noisydissoc entity :type)
    (noisydissoc query :type))
   (l/featurec
    (select-keys entity [:type])
    (select-keys query [:type]))))
