import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

public class Semantico implements Constants {
    private final Map<String, Integer> variaveis = new LinkedHashMap<>();
    private final Deque<Integer> valores = new ArrayDeque<>();
    private final PrintStream saida;
    private String identificadorAtribuicao;
    private Integer resultado;
    private boolean expressaoFinal;

    public Semantico() {
        this(System.out);
    }

    public Semantico(PrintStream saida) {
        this.saida = saida;
    }

    // Cada programa começa com a pilha vazia, mas preserva as variáveis da sessão.
    public void iniciarPrograma() {
        valores.clear();
        identificadorAtribuicao = null;
        resultado = null;
        expressaoFinal = false;
    }

    public void executeAction(int action, Token token) throws SemanticError {
        try {
            switch (action) {
                case 1: // Soma
                case 2: // Subtração
                case 3: // Produto
                case 4: // Quociente inteiro
                case 5: // Potência
                    calcularOperacao(action, token);
                    break;
                case 6: // Logaritmo natural, truncado para inteiro
                    int argumento = retirarValor(token);
                    if (argumento <= 0) {
                        throw erro("Log exige um argumento maior que zero.", token);
                    }
                    valores.push((int) Math.log(argumento));
                    break;
                case 7: // Captura o destino, sem consultar seu valor anterior
                    identificadorAtribuicao = token.getLexeme();
                    break;
                case 8: // Conclui a atribuição
                    if (identificadorAtribuicao == null) {
                        throw erro("Atribuição sem identificador de destino.", token);
                    }

                    resultado = retirarValor(token);
                    variaveis.put(identificadorAtribuicao, resultado);
                    identificadorAtribuicao = null;
                    expressaoFinal = false;
                    break;
                case 9: // Show consome o resultado da expressão
                    resultado = retirarValor(token);
                    expressaoFinal = false;
                    exibirResultado();
                    break;
                case 10: // Apenas os literais aceitos pelo léxico, em base 2
                    valores.push(converterNumero(token));
                    break;
                case 11: // Lê uma variável que já recebeu valor
                    Integer valor = variaveis.get(token.getLexeme());
                    
                    if (valor == null) {
                        throw erro("Variável não declarada: " + token.getLexeme(), token);
                    }

                    valores.push(valor);
                    break;
                default:
                    throw erro("Ação semântica desconhecida: " + action, token);
            }
        } catch (ArithmeticException e) {
            throw erro("Resultado fora do intervalo de inteiros de 32 bits.", token);
        }
    }

    private void calcularOperacao(int acao, Token token) throws SemanticError {
        int direita = retirarValor(token);
        int esquerda = retirarValor(token);
        int valor;

        switch (acao) {
            case 1:
                valor = Math.addExact(esquerda, direita);
                break;
            case 2:
                valor = Math.subtractExact(esquerda, direita);
                break;
            case 3:
                valor = Math.multiplyExact(esquerda, direita);
                break;
            case 4:
                if (direita == 0) {
                    throw erro("Divisão por zero.", token);
                }
                if (esquerda == Integer.MIN_VALUE && direita == -1) {
                    throw new ArithmeticException();
                }
                valor = esquerda / direita;
                break;
            case 5:
                valor = potencia(esquerda, direita, token);
                break;
            default:
                throw erro("Operação desconhecida: " + acao, token);
        }

        valores.push(valor);
    }

    private int potencia(int base, int expoente, Token token) throws SemanticError {
        if (expoente < 0) {
            if (base == 0) {
                throw erro("Zero não pode ser elevado a um expoente negativo.", token);
            }

            if (base == 1) {
                return 1;
            }

            if (base == -1) {
                return (expoente % 2 == 0) ? 1 : -1;
            }

            return 0;
        }

        int valor = 1;
        int fator = base;

        while (expoente > 0) {
            if (expoente % 2 == 1) {
                valor = Math.multiplyExact(valor, fator);
            }

            expoente /= 2;

            if (expoente > 0) {
                fator = Math.multiplyExact(fator, fator);
            }
        }

        return valor;
    }

    private int converterNumero(Token token) throws SemanticError {
        try {
            return Integer.parseInt(token.getLexeme(), 2);
        } 
        catch (NumberFormatException e) {
            throw erro("Literal binário fora do intervalo de 0 a 2147483647.", token);
        }
    }

    private int retirarValor(Token token) throws SemanticError {
        if (valores.isEmpty()) {
            throw erro("Expressão sem operandos suficientes.", token);
        }

        return valores.pop();
    }

    public void finalizarExpressao(Token token) throws SemanticError {
        resultado = retirarValor(token);
        expressaoFinal = true;
    }

    public int getResultado() throws SemanticError {
        if (resultado == null) {
            throw new SemanticError("Nenhuma instrução foi concluída.");
        }

        return resultado;
    }

    public boolean temExpressaoFinal() {
        return expressaoFinal;
    }

    public Map<String, Integer> getVariaveis() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(variaveis));
    }

    public void exibirResultado() throws SemanticError {
        int valor = getResultado();
        String binario = valor < 0? "-" + Long.toBinaryString(-(long) valor) : Integer.toBinaryString(valor);
        saida.println("Resultado: " + valor + " (binário: " + binario + ")");
    }

    private SemanticError erro(String mensagem, Token token) {
        return new SemanticError(mensagem, token == null ? -1 : token.getPosition());
    }
}
