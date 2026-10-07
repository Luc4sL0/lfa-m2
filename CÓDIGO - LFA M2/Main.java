import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Semantico semantico = new Semantico();
        try {
            if (args.length == 1 && args[0].equals("--interativo")) {
                executarConsole(semantico);
            } else if (args.length == 2 && args[0].equals("--arquivo")) {
                if (!executarPrograma(Files.readString(Path.of(args[1]), StandardCharsets.UTF_8),
                        semantico, System.err)) {
                    System.exit(1);
                }
            } else if (args.length > 0 && args[0].startsWith("--")) {
                System.err.println("Uso: Main [\"programa\" | --arquivo caminho | --interativo]");
                System.exit(2);
            } else {
                String programa = args.length == 0
                        ? new String(System.in.readAllBytes(), StandardCharsets.UTF_8)
                        : String.join(" ", args);
                if (!executarPrograma(programa, semantico, System.err)) {
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro de leitura: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void executarConsole(Semantico semantico) throws IOException {
        BufferedReader leitor = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        System.out.println("Calculadora binária. Digite um programa por linha ou :sair.");
        System.out.println("Exemplo: x = 101; Show(x + 1);");
        while (true) {
            System.out.print("> ");
            String programa = leitor.readLine();
            if (programa == null || programa.trim().equals(":sair")) {
                break;
            }
            if (!programa.trim().isEmpty()) {
                executarPrograma(programa, semantico, System.err);
            }
        }
    }

    public static boolean executarPrograma(String programa, Semantico semantico,
            PrintStream erros) {
        try {
            new Sintatico().parse(new Lexico(new StringReader(programa)), semantico);
            if (semantico.temExpressaoFinal()) {
                semantico.exibirResultado();
            }
            return true;
        } catch (AnalysisError e) {
            erros.println(descreverErro(programa, e));
            // Descarta operandos da instrução incompleta, preservando atribuições concluídas.
            semantico.iniciarPrograma();
            return false;
        }
    }

    private static String descreverErro(String programa, AnalysisError erro) {
        String tipo = erro instanceof LexicalError ? "léxico"
                : erro instanceof SyntacticError ? "sintático" : "semântico";
        int posicao = Math.max(0, Math.min(erro.getPosition(), programa.length()));
        int linha = 1;
        int coluna = 1;
        for (int i = 0; i < posicao; i++) {
            if (programa.charAt(i) == '\n') {
                linha++;
                coluna = 1;
            } else {
                coluna++;
            }
        }
        String mensagem = erro instanceof SyntacticError
                ? descreverErroSintatico(programa, posicao, erro.getMessage())
                : erro.getMessage();
        return "Erro " + tipo + " na linha " + linha + ", coluna " + coluna + ": " + mensagem;
    }

    private static String descreverErroSintatico(String programa, int posicao, String mensagem) {
        // Usa o estado informado pelo GALS e sua tabela, sem alterar as mensagens geradas.
        String prefixo = "Erro estado ";
        if (!mensagem.startsWith(prefixo)) {
            return mensagem;
        }
        int estado = Integer.parseInt(mensagem.substring(prefixo.length()));
        String[] nomes = { "fim do programa", "Show", "Log", "identificador", "número binário",
                "=", "+", "-", "*", "/", "**", "(", ")", ";" };
        StringBuilder esperados = new StringBuilder();
        for (int i = 0; i < nomes.length; i++) {
            int comando = Constants.PARSER_TABLE[estado][i][0];
            if (comando == Constants.SHIFT || comando == Constants.REDUCE
                    || comando == Constants.ACTION || comando == Constants.ACCEPT) {
                if (esperados.length() > 0) {
                    esperados.append(", ");
                }
                esperados.append(nomes[i]);
            }
        }
        String encontrado = posicao == programa.length()
                ? "fim do programa" : "'" + programa.charAt(posicao) + "'";
        return "Encontrado " + encontrado + ". Esperado: " + esperados + ".";
    }
}
