import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ExercicioPCM30Alinhamento {
    public static void main(String[] args) {
        // Caminho do arquivo de entrada (com bits)
        String caminhoEntrada = "entrada.txt";

        // Caminho do arquivo de saída (resultado formatado)
        String caminhoSaida = "saida.txt";

        // Padrão de alinhamento da palavra de sincronismo (PAQ)
        String padraoAlinhamento = "10011011";

        try {
            // Lê todo o conteúdo do arquivo de entrada e remove tudo que não for 0 ou 1
            String conteudo = new String(Files.readAllBytes(Paths.get(caminhoEntrada)))
                    .replaceAll("[^01]", "");

            // Acumulador dos resultados finais válidos
            StringBuilder resultadoFinal = new StringBuilder();

            // Índice de busca inicial no conteúdo
            int indice = 0;

            // Loop principal: procura por padrões de alinhamento enquanto houver conteúdo suficiente
            while (indice <= conteudo.length() - 8) {
                // Procura a próxima ocorrência do padrão de alinhamento a partir do índice atual
                int flagPos = conteudo.indexOf(padraoAlinhamento, indice);

                if (flagPos == -1) {
                    // Nenhuma outra flag encontrada, sai do loop
                    break;
                }

                // Verifica se existe espaço suficiente para fazer as validações necessárias
                boolean temEspaco257 = flagPos + 257 < conteudo.length(); // checa o bit 257
                boolean temEspaco512 = flagPos + 512 + padraoAlinhamento.length() <= conteudo.length(); // checa a próxima flag

                if (temEspaco257 && temEspaco512) {
                    // Verifica se o bit 257 a partir da flag é 1
                    boolean bit257 = conteudo.charAt(flagPos + 257) == '1';

                    // Verifica se após 512 bits existe novamente o padrão de alinhamento
                    boolean flagPosterior = conteudo.substring(flagPos + 512, flagPos + 512 + padraoAlinhamento.length())
                            .equals(padraoAlinhamento);

                    // Se ambas condições forem verdadeiras, a flag é considerada válida
                    if (bit257 && flagPosterior) {
                        System.out.println("✅ Flag VÁLIDA em: " + flagPos * 2); // flagPos*2 converte para posição em bits

                        // Extrai exatamente 512 bits a partir da posição da flag
                        String quadro = conteudo.substring(flagPos, flagPos + 512);

                        // Formata os bits adicionando espaço entre cada um
                        StringBuilder comEspaco = new StringBuilder();
                        for (char c : quadro.toCharArray()) {
                            comEspaco.append(c).append(' ');
                        }

                        // Adiciona o quadro processado ao resultado final
                        resultadoFinal.append(comEspaco.toString().trim()).append(" ");
                    } else {
                        // Flag encontrada mas inválida conforme as condições
                        System.out.println("❌ Flag INVÁLIDA em: " + flagPos * 2 +
                                " | bit na posição 257 = " + conteudo.charAt(flagPos + 257) +
                                " | PAQ aparece depois de 512 bits = " + flagPosterior);
                    }

                } else {
                    // Flag foi encontrada mas não há bits suficientes para validar
                    System.out.println("⚠️ Flag em: " + flagPos * 2 + " ignorada por falta de bits para verificar.");
                }

                // Avança para procurar a próxima possível flag (depois da atual)
                indice = flagPos + 8;
            }

            // Se pelo menos um quadro válido foi encontrado
            if (resultadoFinal.length() > 0) {
                // Divide os bits espaçados para organizar a saída por blocos de 128 bits
                String[] bits = resultadoFinal.toString().trim().split(" ");
                StringBuilder formatado = new StringBuilder();

                for (int i = 0; i < bits.length; i++) {
                    formatado.append(bits[i]);

                    // Quebra de linha a cada 128 bits
                    if ((i + 1) % 128 == 0) {
                        formatado.append("\n");
                    } else {
                        formatado.append(" ");
                    }
                }

                // Escreve o resultado formatado no arquivo de saída
                Files.writeString(Paths.get(caminhoSaida), formatado.toString().trim(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                // Nenhum quadro válido foi encontrado
                System.out.println("Nenhum quadro válido encontrado.");
            }

        } catch (IOException e) {
            // Caso haja erro ao ler ou escrever arquivos
            System.err.println("Erro ao ler ou escrever arquivos: " + e.getMessage());
        }
    }
}
