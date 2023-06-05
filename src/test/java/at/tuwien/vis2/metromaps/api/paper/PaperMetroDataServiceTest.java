package at.tuwien.vis2.metromaps.api.paper;

import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PaperMetroDataServiceTest {

    @Test
    void testResourceLoading() {
        PaperMetroDataService service = new PaperMetroDataService();
        List<InputLineEdge> allGeograficEdges = service.getOrderedEdgesForLine("U4", "Vienna");
        List<String> lineNames = service.getAllLineNames("Vienna");
    }
}