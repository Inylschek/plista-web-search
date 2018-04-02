package com.simon.database

abstract class AbstractKeyValueDatabase[K, V] {
  def add(k: K, v: V): Unit

  def queryValues(query: String) : scala.collection.immutable.Map[K, V] 

  def addAll(other: AbstractKeyValueDatabase[K, V]) : Unit
  
  def dropOldData() : Unit
  
  def getTraversable() : TraversableOnce[(K, V)]
}
