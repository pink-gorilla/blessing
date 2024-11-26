(ns blessing.human-id)

(def a ["angelic"
        "blessed"
        "beautiful"
        "celestial"
        "divine"
        "eternal"
        "holy"
        "infinite"
        "immortal"
        "fine"
        "godly"
        "golden"
        "graceful"
        "great"
        "heavenly"
        "holy"
        "honorable"
        "loving"
        "magnificent"
        "memorizable"
        "mighty"
        "powerful"
        "pure"
        "sacred"
        "saintly"
        "spiritual"
        "white"
        "wonderful"
        "wise"])


(def nouns ["angel"
            "beginning"
            "christ"
            "compassion"
            "divinity"
            "earth"
            "eternity"
            "faith"
            "forgiveness"
            "gabriel"
            "grace"
            "god"
            "heaven"
            "holiness"
            "hope"
            "infinity"
            "james"
            "joy"
            "king"
            "light"
            "love"
            "lion"
            "mercy"
            "mind"
            "origin"
            "paul"
            "peace"
            "priest"
            "queen"
            "redemption"
            "saint"
            "saviour"            
            "salvation"
            "soul"
            "spirit"
            "truth"
            "thomas"
            "wizard"])

(defn id []
  (let [a-idx (rand-int (count a))
        n-idx (rand-int (count nouns))]
    (str (a a-idx) "-" (nouns n-idx) "-"
         (rand-int 10))))


(comment
  (id)

;  
  )
