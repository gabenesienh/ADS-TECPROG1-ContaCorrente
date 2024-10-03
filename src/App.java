import java.util.InputMismatchException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        ContaCorrente conta = new ContaCorrente(0, 500);

        while (true) {
            System.out.println(
                  "Escolha uma opção:\n"
                + "1. Exibir saldo\n"
                + "2. Depositar\n"
                + "3. Sacar\n"
                + "4. Transferir\n"
                + "5. Exibir transações\n"
                + "6. Adicionar chave Pix\n"
                + "7. Remover chave Pix\n"
                + "8. Exibir chaves Pix\n"
                + "9. Exibir chaves Pix favoritas\n"
                + "0. Sair"
            );

            int escolha = -1;
            boolean encerrarPrograma = false;

            while (true) {
                try {
                    escolha = teclado.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Escolha inválida.");
                    teclado.nextLine();
                    continue;
                }
                
                teclado.nextLine();
                boolean exibirMenu = true;
                
                switch (escolha) {
                    case 1: exibirMenu = exibirSaldo(conta);                break;
                    case 2: exibirMenu = depositar(teclado, conta);         break;
                    case 3: exibirMenu = sacar(teclado, conta);             break;
                    case 4: exibirMenu = transferir(teclado, conta);        break;
                    case 5: exibirMenu = exibirTransacoes(teclado, conta);  break;
                    case 6: exibirMenu = adicionarPix(teclado, conta);      break;
                    case 7: exibirMenu = removerPix(teclado, conta);        break;
                    case 8: exibirMenu = exibirPix(conta);                  break;
                    case 9: exibirMenu = exibirPixFavoritas(conta);         break;
                    case 0:
                        exibirMenu = false;
                        encerrarPrograma = true;
                        break;
                    default:
                        System.out.println("Escolha inválida.");
                        continue;
                }

                if (!exibirMenu) break; //não mostrar o menu Retornar se o usuário escolheu sair

                while (true) {
                    System.out.println("0. Retornar");

                    while (true) {
                        try {
                            escolha = teclado.nextInt();
                        } catch (InputMismatchException e) {
                            teclado.nextLine();
                            continue;
                        }
                        
                        if (escolha == 0) break;
                    }
                    break;
                }
                break;
            }
            
            if (encerrarPrograma) break;
        }
        
        teclado.close();
    }

    // return true: operação concluída (exibir menu "Retornar")
    // return false: operação cancelada (não exibir menu)
    
    private static boolean exibirSaldo(ContaCorrente conta) {
        System.out.printf(
              "Saldo:  R$%.2f%n"
            + "Limite: R$%.2f%n",
              conta.getSaldo(), conta.getLimite()
        );
        return true;
    }
    
    private static boolean depositar(Scanner teclado, ContaCorrente conta) {
        System.out.print(
              "Informe o valor para depósito:\n"
            + "(Digite \"0\" para retornar)\nR$"
        );
        
        double valor = -1;
        
        while (true) {
            try {
                valor = teclado.nextDouble();
                if (valor == 0) return false;
                conta.depositar(valor);
            } catch (InputMismatchException e) {
                System.out.print("Valor deve ser um número.\nR$");
                teclado.nextLine();
                continue;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                System.out.print("R$");
                continue;
            }
        
            System.out.println("Depósito realizado com sucesso!");
            break;
        }
        
        return true;
    }
    
    private static boolean sacar(Scanner teclado, ContaCorrente conta) {
        System.out.print(
              "Informe o valor para saque:\n"
            + "(Digite \"0\" para retornar)\nR$"
        );
        
        double valor = -1;
        
        while (true) {
            try {
                valor = teclado.nextDouble();
                if (valor == 0) return false;
                conta.sacar(valor);
            } catch (InputMismatchException e) {
                System.out.print("Valor deve ser um número.\nR$");
                teclado.nextLine();
                continue;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                System.out.print("R$");
                continue;
            }
        
            System.out.println("Saque realizado com sucesso!");
            break;
        }
        
        return true;
    }
    
    private static boolean transferir(Scanner teclado, ContaCorrente conta) {
        while (true) {
            System.out.print(
                  "Informe o valor para transferência:\n"
                + "(Digite \"0\" para retornar)\nR$"
            );
            
            double valorTransferencia = 0;
            
            while (true) {
                try {
                    valorTransferencia = teclado.nextDouble();
                    if (valorTransferencia == 0) return false;
                } catch (InputMismatchException e) {
                    System.out.print("Valor deve ser um número.\nR$");
                    teclado.nextLine();
                    continue;
                }
                break;
            }
            
            teclado.nextLine(); //limpar o buffer
            System.out.println(
                  "Informe a chave Pix do destinatário:\n"
                + "(Digite \"0\" para retornar)"
            );
            
            String chave = "";
            
            while (true) {
                try {
                    chave = teclado.nextLine();
                    
                    if (chave.equals("0")) {
                        break;
                    } else {
                        conta.transferir(valorTransferencia, chave);
                        System.out.println("Iniciando transferência...");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Chave inválida.");
                    teclado.nextLine();
                    continue;
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    continue;
                }
                
                System.out.println("Transferência realizada com sucesso!");
                return true;
            }
            continue; //retornar ao primeiro menu se 0 for digitado no segundo
        }
    }
    
    private static boolean exibirTransacoes(Scanner teclado, ContaCorrente conta) {
        System.out.println(
              "Informe quantas transações deseja exibir:\n"
            + "(Digite \"-1\" para exibir todas)"
        );
        
        int numTransacoes = 0;
        
        while (true) {
            try {
                numTransacoes = teclado.nextInt();

                if (numTransacoes == -1) {
                    conta.exibirTransacoes();
                } else {
                    conta.exibirTransacoes(numTransacoes);
                }
            } catch (InputMismatchException e) {
                System.out.println("Valor deve ser um número.");
                teclado.nextLine();
                continue;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                continue;
            }
            break;
        }
        
        return true;
    }
    
    private static boolean adicionarPix(Scanner teclado, ContaCorrente conta) {
        while (true) {
            System.out.println(
                "Informe a chave Pix de 11 números:\n"
                + "(Digite \"0\" para retornar)"
            );

            String chavePix = "";

            while (true) {
                try {
                    chavePix = teclado.nextLine();

                    if (chavePix.equals("0")) return false;
                } catch (InputMismatchException e) {
                    System.out.println("Chave inválida.");
                    teclado.nextLine();
                    continue;
                }

                System.out.println(
                    "Digite uma descrição para a chave " + chavePix + '\n'
                    + "(Digite \"0\" para retornar, ou \"-1\" para não informar descrição)"
                );

                String descricao = "";

                while (true) {
                    try {
                        descricao = teclado.nextLine();

                        if (descricao.equals("0")) break;

                        if (descricao.equals("-1")) {
                            conta.adicionarChavePix(chavePix);
                        } else {
                            conta.adicionarChavePix(chavePix, descricao);
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Descrição inválida.");
                        teclado.nextLine();
                        continue;
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                        continue;
                    }

                    System.out.println("Chave Pix adicionada com sucesso!");
                    return true;
                }
                break;
            }
            continue;
        }
    }
    
    private static boolean removerPix(Scanner teclado, ContaCorrente conta) {
        System.out.println(
              "Informe a chave para remover:\n"
            + "(Digite \"0\" para retornar)"
        );

        String chavePix = "";

        while (true) {
            try {
                chavePix = teclado.nextLine();

                if (chavePix.equals("0")) return false;
            } catch (InputMismatchException e) {
                System.out.println("Chave inválida.");
                teclado.nextLine();
                continue;
            }

            if (conta.removerChavePix(chavePix)) {
                System.out.println("Chave Pix removida com sucesso!");
                return true;
            } else {
                System.out.println("A chave Pix não foi encontrada.");
                continue;
            }
        }
    }
    
    private static boolean exibirPix(ContaCorrente conta) {
        conta.exibirChavesPix();
        return true;
    }
    
    private static boolean exibirPixFavoritas(ContaCorrente conta) {
        conta.exibirChavesPixFavoritas();
        return true;
    }
}