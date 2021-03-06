// Copyright 2017 MPI-SWS, Saarbruecken, Germany

package daisy
package search

import util.Random
import scala.collection.immutable.Seq

import lang.Trees.Expr
import tools.Rational
import Rational._

// The MutationRules should probably not be mixed-in to the generic genetic program
trait GeneticSearch[T] {

  var maxGenerations = 30
  private var _populationSize = 30
  def populationSize_=(i: Int): Unit = {
    require(i % 2 == 0)
    _populationSize = i
  }
  def populationSize: Int = _populationSize
  val tournamentSize = 4

  // NOTE: this is mutable (so that the value can be set by the user with command-line)
  var rand: Random

  var reporter: Reporter
  implicit val debugSection: DebugSection

  /** This is a general genetic algorithm.

    @param inputExpr expression to rewrite
    @param copy function to perform a deep copy
    @param fitnessFnc fitness function to guide the search, assumes that smaller is better
    @return AST and fitness value of best expression found)
   */
  def runGenetic(inputExpr: T, copy: T => T, fitnessFnc: T => Rational): (T, Rational) = {

    val initialFitness = fitnessFnc(inputExpr)

    // initialize population: expr with its fitness value
    var currentPopulation: Seq[(T, Rational)] =
      Array.fill(populationSize)((copy(inputExpr), initialFitness)).toList

    var globalBest: (T, Rational) = (inputExpr, initialFitness)

    for (i <- 0 until maxGenerations) {
      assert(currentPopulation.size == populationSize)
      val newPopulation: Seq[(T, Rational)] = (0 until populationSize/2).flatMap { _ =>

        // select two individuals from population (2 to enable crossover later)
        val candOne = tournamentSelect(currentPopulation)
        val candTwo = tournamentSelect(currentPopulation)

        // mutate individuals
        val mutantOne = mutate(candOne)
        val mutantTwo = mutate(candTwo)

        // TODO: crossover (with a certain probability)

        // evaluate fitness of new candidates
        Seq((mutantOne, fitnessFnc(mutantOne)), (mutantTwo, fitnessFnc(mutantTwo)))

      }
      // update new population
      currentPopulation = newPopulation

      // check if we have found a better expression; compare the fitness values
      val currentBest = currentPopulation.sortWith(_._2 < _._2).head
      if (currentBest._2 < globalBest._2) {
        globalBest = currentBest
      }
    }

    globalBest
  }

  def tournamentSelect(list: Seq[(T, Rational)]): T = {

    val candidates = rand.shuffle(list).take(tournamentSize)

    // sort by fitness value - smaller is better and pick first
    candidates.sortWith(_._2 < _._2).head._1

  }

  def mutate(expr: T): T
}