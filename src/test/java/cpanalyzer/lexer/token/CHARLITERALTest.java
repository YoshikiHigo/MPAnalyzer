package cpanalyzer.lexer.token;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import yoshikihigo.cpanalyzer.lexer.token.CHARLITERAL;

public class CHARLITERALTest {

  @Test
  public void testQuotation() {
    final CHARLITERAL literal = new CHARLITERAL("c");
    assertThat(literal.value).isEqualTo("\'c\'");
  }
}
