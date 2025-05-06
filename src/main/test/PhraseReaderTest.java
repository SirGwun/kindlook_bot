import com.application.Model.Phrase;
import com.application.serves.PhraseReader;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PhraseReaderXmlTest {

    private static final String TEST_FILE_PATH = "test_phrases.xml";
    private static final PhraseReader phraseReader = new PhraseReader(TEST_FILE_PATH);

    @BeforeEach
    void setUp() throws IOException {
        String xmlContent = """
                <batton name="Главная">
                    <phrase>Привет</phrase>
                    <phrase>Пока</phrase>
                    <batton name="Вложенная">
                        <phrase>Внутри вложенной</phrase>
                    </batton>
                </batton>
                """;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(TEST_FILE_PATH), StandardCharsets.UTF_8))) {
            writer.write(xmlContent);
        }
    }

    @AfterEach
    void tearDown() {
        new File(TEST_FILE_PATH).delete();
    }

    @Test
    void testReadAllPhrasesFromXml() {
        phraseReader.setFilePatch(TEST_FILE_PATH);
        List<Phrase> phrases = phraseReader.readAllPhrases();

        assertNotNull(phrases, "Список фраз не должен быть null");
        assertEquals(3, phrases.size(), "Должно быть три фразы");

        assertEquals("Привет", phrases.get(0).getText());
        assertEquals("Главная", phrases.get(0).getButtonText());

        assertEquals("Пока", phrases.get(1).getText());
        assertEquals("Главная", phrases.get(1).getButtonText());

        assertEquals("Внутри вложенной", phrases.get(2).getText());
        assertEquals("Вложенная", phrases.get(2).getButtonText());
    }

    @Test
    void testEmptyXmlFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE_PATH, false))) {
            writer.write("");
        }

        phraseReader.setFilePatch(TEST_FILE_PATH);
        List<Phrase> phrases = phraseReader.readAllPhrases();

        assertNotNull(phrases, "Список фраз не должен быть null");
        assertTrue(phrases.isEmpty(), "Список должен быть пустым");
    }

    @Test
    void testInvalidXmlStructure() throws IOException {
        String invalidXml = "<batton name=\"Oops\"><phrase>Незакрытый тег";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE_PATH, false))) {
            writer.write(invalidXml);
        }

        phraseReader.setFilePatch(TEST_FILE_PATH);
        assertThrows(RuntimeException.class, phraseReader::readAllPhrases, "Должно выбрасываться исключение при некорректной структуре XML");
    }
}
