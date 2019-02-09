package csense.example.app

import csense.example.app.generated.*
import csense.example.app.test.*
import javafx.application.*
import javafx.event.*
import javafx.scene.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.*


class HelloWorld : Application() {

    private val binding: testBinding = testBinding()

    override fun start(primaryStage: Stage) {
//        val root = StackPane()
//        root.children.add(btn)

        val scene = Scene(binding.root, 300.0, 250.0)
        binding.clickMe.setOnAction {
            println(Strings.main.i0)
            binding.myId.text += "*"
        }

        primaryStage.title = "Hello World!"
        primaryStage.scene = scene
        primaryStage.show()
    }

}

fun main(args: Array<String>) {
    Application.launch(HelloWorld::class.java, *args)
}