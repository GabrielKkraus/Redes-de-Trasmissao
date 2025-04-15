import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class AlinhamentoPCMContinuo {
    public static void main(String[] args) {
        // Caminhos para o arquivo de entrada e saída
        String caminhoEntrada = "entrada.txt";
        String caminhoSaida = "saida.txt";
        
        // Padrão de alinhamento que estamos buscando nos dados
        String padraoAlinhamento = "10011011";

        try {
            // Lê o conteúdo do arquivo de entrada, removendo todos os caracteres que não sejam '0' ou '1'
            String conteudo = new String(Files.readAllBytes(Paths.get(caminhoEntrada)))
                    .replaceAll("[^01]", "");

            // StringBuilder para armazenar o resultado final
            StringBuilder resultadoFinal = new StringBuilder();
            int indice = 0; // Variável para controlar a posição da busca

            // Loop para encontrar as flags e processar os quadros válidos
            while (indice <= conteudo.length() - 8) {
                // Encontra a posição da primeira ocorrência do padrão de alinhamento
                int flagPos = conteudo.indexOf(padraoAlinhamento, indice);

                // Se não encontrar o padrão ou não houver dados suficientes para verificar a posição 257, sai do loop
                if (flagPos == -1 || flagPos + 257 >= conteudo.length()) {
                    break;
                }

                // Verifica se o bit na posição 257 é '1' e se o padrão de alinhamento aparece novamente após 512 bits
                if (flagPos + 512 + padraoAlinhamento.length() <= conteudo.length() &&
                    conteudo.charAt(flagPos + 257) == '1' && 
                    conteudo.substring(flagPos + 512, flagPos + 512 + padraoAlinhamento.length()).equals(padraoAlinhamento)) {
                    
                    // Caso o quadro seja válido, exibe a posição da flag válida
                    System.out.println("Flag válida em: " + flagPos);
                    
                    // Extrai o quadro a partir da posição da flag até o fim do conteúdo
                    int fimQuadro = conteudo.length(); // Pega até o fim do conteúdo
                    String extraido = conteudo.substring(flagPos, fimQuadro);

                    // Formata a saída para colocar espaços entre os bits extraídos
                    StringBuilder comEspaco = new StringBuilder();
                    for (char c : extraido.toCharArray()) {
                        comEspaco.append(c).append(' ');
                    }

                    // Adiciona os dados formatados ao resultado final
                    resultadoFinal.append(comEspaco.toString().trim()).append(" ");
                    break; // Se quiser parar no primeiro quadro válido, usa `break`; caso contrário, remove esse comando
                } else {
                    // Se a flag não for válida, move o índice para a próxima posição após o padrão encontrado
                    System.out.println("Flag inválida em: " + flagPos);
                    indice = flagPos + 8;
                }
            }

            // Verifica se algum quadro válido foi encontrado antes de tentar escrever no arquivo de saída
            if (resultadoFinal.length() > 0) {
                // Divide os bits em blocos de 128 bits para formatar a saída
                String[] bits = resultadoFinal.toString().trim().split(" ");
                StringBuilder formatado = new StringBuilder();
                for (int i = 0; i < bits.length; i++) {
                    formatado.append(bits[i]);
                    // A cada 128 bits, adiciona uma nova linha
                    if ((i + 1) % 128 == 0) {
                        formatado.append("\n");
                    } else {
                        formatado.append(" ");
                    }
                }

                // Escreve os dados formatados no arquivo de saída
                Files.writeString(Paths.get(caminhoSaida), formatado.toString().trim(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                // Caso nenhum quadro válido tenha sido encontrado
                System.out.println("Nenhum quadro válido encontrado.");
            }

        } catch (IOException e) {
            // Trata erros de leitura ou escrita nos arquivos
            System.err.println("Erro ao ler ou escrever arquivos: " + e.getMessage());
        }
    }
}
