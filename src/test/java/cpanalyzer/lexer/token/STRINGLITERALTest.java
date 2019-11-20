package cpanalyzer.lexer.token;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import yoshikihigo.cpanalyzer.lexer.token.STRINGLITERAL;

public class STRINGLITERALTest {

  @Test
  public void testQuotation() {    
    final STRINGLITERAL literal = new STRINGLITERAL(" string literal ");
    assertThat(literal.value).isEqualTo("\" string literal \"");
  }
}
