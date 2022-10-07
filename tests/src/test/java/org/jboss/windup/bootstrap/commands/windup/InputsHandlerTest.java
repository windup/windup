package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.util.exception.WindupException;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class InputsHandlerTest {

    private InputsHandler inputsHandler = new InputsHandler();

    @Test(expected = WindupException.class)
    public void handleShouldThrowExceptionWhenNoInputsGiven() {
        inputsHandler.handle(new ArrayList<>());
    }

    @Test
    public void shouldReturnArchivesWhenInputIsFolderWithArchives() {
        List<Path> inputs = new ArrayList<>();
        inputs.add(Path.of("../test-files/duplicate"));

        List<Path> result = inputsHandler.handle(inputs);

        assertEquals(3, result.size());
    }

    @Test
    public void shouldReturnASingleInputWhenInputIsAFolderWithNoArchives() {
        List<Path> inputs = new ArrayList<>();
        inputs.add(Path.of("../test-files/src_example"));

        List<Path> result = inputsHandler.handle(inputs);

        assertEquals(1, result.size());
    }

    @Test
    public void shouldReturnInputsWhenThereAreMultipleInputs() {
        List<Path> inputs = new ArrayList<>();
        inputs.add(Path.of("../test-files/src_example"));
        inputs.add(Path.of("../test-files/jee-example-app-1.0.0.ear"));

        List<Path> result = inputsHandler.handle(inputs);

        assertEquals(2, result.size());
    }

}