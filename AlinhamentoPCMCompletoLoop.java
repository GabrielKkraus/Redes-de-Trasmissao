import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class AlinhamentoPCMCompletoLoop {
    public static void main(String[] args) {
        // Caminho do arquivo de entrada e saída
        String caminhoEntrada = "entrada.txt"; // Substitua pelo nome do seu arquivo de entrada
        String caminhoSaida = "saida.txt";      // Nome do arquivo que será gerado com os resultados

        // Definições das flags de sincronismo
        String PAQ = "10011011";      // Flag de alinhamento de quadro (PCM30)
        String PAMQPrefixo = "0000";  // Prefixo da flag de multiquadro

        try {
            // Lê todo o conteúdo do arquivo de entrada, mantendo apenas os bits binários (0 e 1)
            String conteudo = new String(Files.readAllBytes(Paths.get(caminhoEntrada)))
                    .replaceAll("[^01]", ""); // Remove qualquer caractere que não seja 0 ou 1

            StringBuilder saida = new StringBuilder(); // Armazena o conteúdo que será gravado no arquivo de saída
            int indice = 0;                            // Posição atual no texto
            int contadorMultiquadros = 0;              // Contador de multiquadros válidos encontrados

            // Enquanto houver espaço para pelo menos 1 multiquadro (4096 bits)
            while (indice != -1 && indice + 4096 <= conteudo.length()) {
                // Busca o próximo índice da flag PAQ
                indice = conteudo.indexOf(PAQ, indice);

                // Se não encontrou ou não há espaço para um multiquadro completo, encerra
                if (indice == -1 || indice + 4096 > conteudo.length()) {
                    break;
                }

                // Verifica se o bit na posição 257 após o PAQ é igual a 1
                boolean bit257Valido = conteudo.charAt(indice + 257) == '1';

                // Verifica se há uma segunda ocorrência de PAQ exatamente 512 bits após a primeira
                boolean segundaPAQValida = conteudo.substring(indice + 512, indice + 520).equals(PAQ);

                // Lê os 8 bits logo após a segunda PAQ (PAMQ) e verifica o prefixo
                String pamq = conteudo.substring(indice + 520, indice + 528);

                // Se todas as validações forem verdadeiras, temos um multiquadro válido
                if (bit257Valido && segundaPAQValida && pamq.startsWith(PAMQPrefixo)) {
                    contadorMultiquadros++;

                    // Adiciona mensagem de identificação no arquivo de saída
                    saida.append("🔹 Multiquadro válido #").append(contadorMultiquadros)
                         .append(" encontrado no índice: ").append(indice).append("\n");

                    // Itera sobre os 16 quadros de 256 bits cada
                    for (int i = 0; i < 16; i++) {
                        // Calcula onde começa o quadro real no conteúdo total:
                        // 528 bits após o índice inicial (PAQ) é onde começa o primeiro quadro
                        int inicioQuadro = indice + 528 + i * 256;
                        int fimQuadro = inicioQuadro + 256;

                        // Segurança para evitar erro de substring se ultrapassar o tamanho
                        if (fimQuadro > conteudo.length()) break;

                        // Extrai o quadro completo
                        String quadro = conteudo.substring(inicioQuadro, fimQuadro);

                        // TS16 está localizado entre os bits 120 e 127 do quadro (15º byte)
                        int inicioTS16 = 15 * 8;
                        String ts16 = quadro.substring(inicioTS16, inicioTS16 + 8);

                        // Extrai os bits de sinalização: b0, b1, b4, b5
                        char b0 = ts16.charAt(0);
                        char b1 = ts16.charAt(1);
                        char b4 = ts16.charAt(4);
                        char b5 = ts16.charAt(5);

                        // Adiciona à saída as informações do quadro e dos bits de sinalização
                        saida.append(String.format("Quadro %02d: TS16 = %s | b0=%c b1=%c b4=%c b5=%c\n",
                                i + 1, ts16, b0, b1, b4, b5));
                    }

                    // Adiciona quebra de linha entre os multiquadros
                    saida.append("\n");

                    // Avança o índice para o final deste multiquadro, evitando sobreposição
                    indice += 4096;
                } else {
                    // Caso inválido, apenas exibe no console para depuração
                    System.out.println("❌ Flag inválida em índice: " + indice);
                    indice += 8; // Avança 1 byte para continuar a busca
                }
            }

            // Se nenhum multiquadro válido foi encontrado, adiciona aviso no arquivo
            if (contadorMultiquadros == 0) {
                saida.append("⚠️ Nenhum multiquadro válido encontrado.\n");
            }

            // Escreve o conteúdo formatado no arquivo de saída
            Files.writeString(Paths.get(caminhoSaida), saida.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("✅ Processamento completo. Multiquadros encontrados: " + contadorMultiquadros);

        } catch (IOException e) {
            // Captura erros de leitura ou escrita no sistema de arquivos
            System.err.println("Erro ao ler ou escrever arquivos: " + e.getMessage());
        }
    }
}
