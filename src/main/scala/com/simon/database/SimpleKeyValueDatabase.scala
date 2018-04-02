package com.simon.database

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import scala.collection.TraversableOnce

// TODO convert to immutable type
class SimpleKeyValueDatabase[K, V](f:(K, V, String) => Boolean) extends AbstractKeyValueDatabase[K, V] {
  val map: Map[K, V] = new HashMap[K, V]

  def add(k: K, v: V): Unit = {
    map += (k -> v)
  }

  def queryValues(query: String) : scala.collection.immutable.Map[K, V] = {
    map.filter((kv: (K, V)) => f(kv._1, kv._2, query)).toMap
  }

  def addAll(other: AbstractKeyValueDatabase[K, V]) : Unit = {
    map ++= other.getTraversable
  }
  
  def getTraversable() : TraversableOnce[(K, V)] = map

  // TODO implement and use this method
  def dropOldData() : Unit = {}
  
}