@file:Suppress("UNUSED_PARAMETER")

package lesson12.task1

import ru.spbstu.kotlin.typeclass.classes.Monoid.Companion.plus
import java.lang.IllegalArgumentException


/**
 * Класс "расписание поездов".
 *
 * Общая сложность задания -- средняя, общая ценность в баллах -- 16.
 * Объект класса хранит расписание поездов для определённой станции отправления.
 * Для каждого поезда хранится конечная станция и список промежуточных.
 * Поддерживаемые методы:
 * добавить новый поезд, удалить поезд,
 * добавить / удалить промежуточную станцию существующему поезду,
 * поиск поездов по времени.
 *
 * В конструктор передаётся название станции отправления для данного расписания.
 */
class TrainTimeTable(private val baseStationName: String) {

    data class Route(val train: String, var depart: Time, val destination: Stop)

    private var array = arrayListOf<Train>()

    /**
     * Добавить новый поезд.
     *
     * Если поезд с таким именем уже есть, следует вернуть false и ничего не изменять в таблице
     *
     * @param train название поезда
     * @param depart время отправления с baseStationName
     * @param destination конечная станция
     * @return true, если поезд успешно добавлен, false, если такой поезд уже есть
     */
    private fun stopDepart(depart: Time): Stop = Stop(baseStationName, depart)

    fun addTrain(train: String, depart: Time, destination: Stop): Boolean {
        val check = array.find { it.name == train }
        return if (check != null) {
            false
        } else {
            array.add(Train(train, stopDepart(depart), destination))
            true
        }
    }

    /**
     * Удалить существующий поезд.
     *
     * Если поезда с таким именем нет, следует вернуть false и ничего не изменять в таблице
     *
     * @param train название поезда
     * @return true, если поезд успешно удалён, false, если такой поезд не существует
     */
    fun removeTrain(train: String): Boolean {
        val check = array.find { it.name == train }
        return if (check == null)
            false
        else {
            array.remove(check)
            true
        }
    }

    /**
     * Добавить/изменить начальную, промежуточную или конечную остановку поезду.
     *
     * Если у поезда ещё нет остановки с названием stop, добавить её и вернуть true.
     * Если stop.name совпадает с baseStationName, изменить время отправления с этой станции и вернуть false.
     * Если stop совпадает с destination данного поезда, изменить время прибытия на неё и вернуть false.
     * Если stop совпадает с одной из промежуточных остановок, изменить время прибытия на неё и вернуть false.
     *
     * Функция должна сохранять инвариант: время прибытия на любую из промежуточных станций
     * должно находиться в интервале между временем отправления с baseStation и временем прибытия в destination,
     * иначе следует бросить исключение IllegalArgumentException.
     * Также, время прибытия на любую из промежуточных станций не должно совпадать с временем прибытия на другую
     * станцию или с временем отправления с baseStation, иначе бросить то же исключение.
     *
     * @param train название поезда
     * @param stop начальная, промежуточная или конечная станция
     * @return true, если поезду была добавлена новая остановка, false, если было изменено время остановки на старой
     */
    fun addStop(train: String, stop: Stop): Boolean {
        var result = false
        for (i in array.indices) {
            if (train == array[i].name) {
                val destination = array[i].stops[array[i].stops.size - 1]
                if (baseStationName == stop.name) {
                    for (a in 1 until array[i].stops.size - 1)
                        if (array[i].stops[a].time == stop.time || array[i].stops[a].time.compareTo(stop.time) == -1) throw IllegalArgumentException()
                        else {
                            array[i].stops[0].time = stop.time
                            result = false
                        }
                } else if (array[i].stops[array[i].stops.size - 1].name == stop.name) {
                    for (a in 0 until array[i].stops.size - 2)
                        if (array[i].stops[a].time == stop.time || array[i].stops[a].time.compareTo(stop.time) == 1) throw IllegalArgumentException()
                        else {
                            array[i].stops[array[i].stops.size - 1].time = stop.time
                            result = false
                        }
                } else {

                    for (a in array[i].stops.indices) {
                        if (array[i].stops[a].time == stop.time || array[i].stops[0].time.compareTo(stop.time) == 1 || array[i].stops[a].time.compareTo(stop.time) == -1)
                            throw IllegalArgumentException()
                        if (array[i].stops[a].name.contains(stop.name)) {
                            array[i].stops[a].time = stop.time
                            result = false
                            break

                        } else {
                            array[i].stops.
                            result = true
                            break
                        }
                    }
                }
            }
        }
        return result
    }

    /**
     * Удалить одну из промежуточных остановок.
     *
     * Если stopName совпадает с именем одной из промежуточных остановок, удалить её и вернуть true.
     * Если у поезда нет такой остановки, или stopName совпадает с начальной или конечной остановкой, вернуть false.
     *
     * @param train название поезда
     * @param stopName название промежуточной остановки
     * @return true, если удаление успешно
     */
    fun removeStop(train: String, stopName: String): Boolean = TODO()

    /**
     * Вернуть список всех поездов, упорядоченный по времени отправления с baseStationName
     */
    fun trains(): List<Train> = TODO()

    /**
     * Вернуть список всех поездов, отправляющихся не ранее currentTime
     * и имеющих остановку (начальную, промежуточную или конечную) на станции destinationName.
     * Список должен быть упорядочен по времени прибытия на станцию destinationName
     */
    fun trains(currentTime: Time, destinationName: String): List<Train> = TODO()

    /**
     * Сравнение на равенство.
     * Расписания считаются одинаковыми, если содержат одинаковый набор поездов,
     * и поезда с тем же именем останавливаются на одинаковых станциях в одинаковое время.
     */
    override fun equals(other: Any?): Boolean =
        other is TrainTimeTable && (array == other.array)


}

/**
 * Время (часы, минуты)
 */
data class Time(val hour: Int, val minute: Int) : Comparable<Time> {
    /**
     * Сравнение времён на больше/меньше (согласно контракту compareTo)
     */
    override fun compareTo(other: Time): Int {
        return when {
            this.hour < other.hour || (this.hour == other.hour && this.minute < other.minute) -> -1
            this.hour > other.hour || (this.hour == other.hour && this.minute > other.minute) -> 1
            else -> 0
        }
    }
}

/**
 * Остановка (название, время прибытия)
 */
data class Stop(val name: String, var time: Time)

/**
 * Поезд (имя, список остановок, упорядоченный по времени).
 * Первой идёт начальная остановка, последней конечная.
 */
data class Train(val name: String, val stops: List<Stop>) {
    constructor(name: String, vararg stops: Stop) : this(name, stops.asList())

    fun removing(train: Train, stop: Stop): Train {
        val destination = train.stops.last()
        val list = train.stops as MutableList<Stop>
        train.stops.removeAt(stops.size - 1)
        train.stops.add(stop)
        train.stops.add(destination)
        return train
    }
}



