package csense.example.app.test

import javafx.fxml.*
import javafx.scene.control.*
import javafx.scene.layout.*

class TestBinding {

    val root: VBox

    val myId: Label
    val clickMe: Button

    init {
        root = FXMLLoader.load(resourceUri)
        //TODO do in background thread ? as we are only searching though the FXML ?
        // or should we generate the annotation ?
        // hmm
        myId = root.lookup("#myId") as Label
        clickMe = root.lookup("#clickMe") as Button

    }

    companion object {
        private val resourcePath: String = "test.fxml"
        private val resourceUri = TestBinding::class.java.classLoader.getResource(resourcePath)
    }


}