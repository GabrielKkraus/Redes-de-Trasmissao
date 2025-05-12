import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class AlinhamentoPCMContinuo {
    public static void main(String[] args) {
        // Caminho do arquivo de entrada e saída
        String caminhoEntrada = "teste.txt";
        String caminhoSaida = "saida.txt";
        
        // Padrão da flag de alinhamento do sistema PCM30 (também chamado de PAQ)
        String padraoAlinhamento = "10011011";

        try {
            // Lê todo o conteúdo do arquivo de entrada e remove tudo que não for '0' ou '1'
            String conteudo = new String(Files.readAllBytes(Paths.get(caminhoEntrada)))
                    .replaceAll("[^01]", ""); // Mantém apenas bits binários

            // Chama o método para encontrar a primeira ocorrência válida da sequência
            String resultado = encontrarSubstringValida(conteudo, padraoAlinhamento);

            // Verifica se foi encontrado algum quadro válido
            if (!resultado.equals("Erro")) {
                // Adiciona espaço entre cada bit para facilitar visualização
                String comEspaco = resultado.replaceAll("", " ").trim();

                // Cria um builder para formatar a saída com quebra de linha a cada 128 bits
                StringBuilder quebrado = new StringBuilder();
                String[] bits = comEspaco.split(" ");
                for (int i = 0; i < bits.length; i++) {
                    quebrado.append(bits[i]);
                    
                    // A cada 128 bits, insere uma quebra de linha
                    if ((i + 1) % 128 == 0) {
                        quebrado.append("\n");
                    } else {
                        quebrado.append(" ");
                    }
                }

                // Escreve o conteúdo formatado no arquivo de saída
                Files.writeString(Paths.get(caminhoSaida), quebrado.toString().trim(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                // Caso nenhuma flag válida seja encontrada, exibe mensagem
                System.out.println("Nenhuma flag válida foi encontrada.");
            }

        } catch (IOException e) {
            // Trata erro de leitura ou escrita de arquivo
            System.err.println("Erro ao ler ou escrever arquivos: " + e.getMessage());
        }
    }

    /**
     * Método que busca a primeira flag de alinhamento válida dentro do conteúdo binário.
     * 
     * Regras:
     *  - A flag deve ser igual a "10011011"
     *  - O bit na posição 257 após o início da flag deve ser igual a '1'
     *  - Deve existir uma segunda ocorrência da mesma flag exatamente 512 bits após a primeira
     *
     * @param texto O conteúdo completo em binário, já limpo
     * @param PAQ O padrão de alinhamento (flag)
     * @return A substring do conteúdo a partir da primeira flag válida ou "Erro" se não houver
     */
    private static String encontrarSubstringValida(String texto, String PAQ) {
        int indice = 0; // Começa buscando a partir do início

        while (true) {
            // Procura a próxima ocorrência da flag
            indice = texto.indexOf(PAQ, indice);

            // Se a flag for encontrada e houver pelo menos 512 bits depois dela
            if (indice != -1 && texto.length() > indice + 512) {
                // Verifica se o bit na posição 257 após o início da flag é '1'
                if (texto.charAt(indice + 257) == '1') {
                    // Verifica se há uma segunda flag exatamente 512 bits à frente
                    if (texto.indexOf(PAQ, indice + 8) == indice + 512) {
                        System.out.println("✅ Flag válida em: " + (indice * 2)); // Multiplica por 2 para exibir em posição de bit real
                        return texto.substring(indice); // Retorna a partir da flag válida até o fim do conteúdo
                    }
                }
                // Caso a flag seja inválida, imprime posição e continua procurando
                System.out.println("❌ Flag inválida em: " + (indice * 2));
                indice = indice + 8; // Avança 8 bits para não ficar preso na mesma posição
            } else {
                // Se não há mais espaço ou flag encontrada, finaliza
                System.out.println("⚠️ Nenhuma flag válida encontrada.");
                return "Erro";
            }
        }
    }
}
