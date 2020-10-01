package com.github.kindrat.openfx.testing.junit

import com.github.kindrat.cassandra.client.CassandraClient
import com.github.kindrat.openfx.testing.TestSubject
import javafx.application.Platform
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.robot.Robot
import org.junit.jupiter.api.extension.*
import java.awt.Point
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.awt.image.Raster
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class OpenFxTestExtension : BeforeEachCallback, AfterEachCallback, ParameterResolver {
    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create(OpenFxTestExtension::class.java)
    }

    override fun beforeEach(context: ExtensionContext) {
        val testApplication = TestSubject.create(CassandraClient::class.java)
        testApplication.awaitVisible()
        context.getStore(NAMESPACE).put(TestSubject::class.java, testApplication)
    }

    override fun afterEach(context: ExtensionContext) {
        val testApplication = context.getStore(NAMESPACE).get(TestSubject::class.java) as TestSubject<*>
        val future = CompletableFuture<File>()
        Platform.runLater {
            val robot = Robot()
            val visualBounds = testApplication.visualBounds()

            val width = visualBounds.width.toInt()
            val height = visualBounds.height.toInt()

            val writableImage = WritableImage(width, height)
            val bufferedImage = robot.getScreenCapture(writableImage, visualBounds)
            val size = width * height * 4
            val intArray = IntArray(size)
            bufferedImage.pixelReader.getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), intArray, 0, width)
            val file = File("screen-capture.png")
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            image.data = Raster.createRaster(image.sampleModel, DataBufferInt(intArray, intArray.size), Point())
            ImageIO.write(image, "png", file)
            future.complete(file)
        }
        future.get(1, TimeUnit.MINUTES)
        testApplication.stop()
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.isAnnotationPresent(OpenFx::class.java)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return extensionContext.getStore(NAMESPACE).get(TestSubject::class.java) as TestSubject<*>
    }
}