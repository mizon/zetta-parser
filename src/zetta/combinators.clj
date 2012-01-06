(ns zetta.combinators
  (:refer-clojure :exclude [replicate])
  (:require [clojure.core :as core])
  (:use [clojure.algo.monads :only [m-seq]])

  (:use zetta.core))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Parser combinators
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn <?>
  "Allows to add an error message to a given parser p."
  [p err-msg]
  (fn [i0 m0 ff sf]
    (letfn [
      (new-ff [i0 m0 errors msg]
        (ff i0 m0 (conj errors err-msg) msg))]
    (p i0 m0 new-ff sf))))

(declare many-v)

(defn- some-v [p]
  (do-parser
    [h p
     t (many-v p)] (cons h t)))

(defn- many-v [p]
  (with-parser
    (<|> (some-v p) (m-result []))))

(defn many
  "Applies zero or more times a parser p."
  [p] (many-v p))

(defn choice [ps]
  "It will try to parse the input using each of the
  given parsers, it will halt on the first parser that
  successfuly parse the input."
  (with-parser
    (reduce <|> ps)))

(defn replicate
  "Apply the given parser 'p' 'n' times, returning every result."
  [n p]
  (with-parser
    (m-seq (core/replicate n p))))

(defn option
  "Applies parser p to the input, if p fails then default-val
  is returned."
  [default-val p]
  (with-parser
    (<|> p (m-result default-val))))

(defn many1
  "Applies one or more times a parser p."
  [p]
  (with-parser
    (<$> cons p (many p))))

(defn around
  [sep content]
  (with-parser
    (*> sep (<* content sep))))

(defn sep-by1
  "Applies one or more times the parser p separated by parser s."
  [p s]
  (with-parser
    (<$> cons
         p
         (<|> (*> s (sep-by1 p s))
              (m-result [])))))

(defn sep-by
  "Applies zero or more times the parser p separated by parser s."
  [p s]
  (with-parser
    (<|>  (<$> cons
               p
               (<|> (*> s (sep-by1 p s))
                    (m-result [])))
          (m-result []))))

(defn many-till
  "Applies the parser p zero or more times until the parser end
  is successful."
  [p end]
  (with-parser
    (<|> (*> end (m-result []))
         (>>= p (fn [h]
         (>>= (many-till p end) (fn [t]
         (m-result (cons h t)))))))))

(defn skip-many
  "Skip zero or more applications of parser p."
  [p]
  (with-parser
    (<|> (*> p (skip-many p))
         (m-result []))))

(defn skip-many1
  "Skip one or more applications of parser p."
  [p]
  (with-parser
    (*> p (skip-many p))))
