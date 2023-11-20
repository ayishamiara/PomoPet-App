package com.mobdeve.chuachingdytocstamaria.mco.pomopet.models

class ToDo {
    var id: Int = -1
    var isDone: Boolean
    var label: String
    constructor(){
        this.label = ""
        this.isDone = false
    }

    constructor(id: Int, label: String){
        this.id = id
        this.label = label
        this.isDone = false
    }

    constructor(label: String){
        this.label = label
        this.isDone = false
    }

}