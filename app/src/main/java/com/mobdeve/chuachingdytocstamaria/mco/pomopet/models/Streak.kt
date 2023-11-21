package com.mobdeve.chuachingdytocstamaria.mco.pomopet.models

class Streak {
    var id: Int = -1
    var date: String = ""
    var cycle_num: Int = -1

    constructor(){
        this.date = ""
        this.cycle_num = 0
    }

    constructor(date: String){
        this.date = date
    }

    constructor(id:Int, date: String, cycle: Int){
        this.id = id
        this.cycle_num = cycle
        this.date = date
    }
}