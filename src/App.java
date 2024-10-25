import java.util.InputMismatchException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        ContaCorrenteDAO contaDAO = new ContaCorrenteDAO();
        contaDAO.criarTabelas();

        ContaCorrente conta = null;

        // Menu inicial
        // Como regra geral, todos os menus usam o input "0" para sair
        while (true) {
            System.out.println("Informe o número da conta:");
            
            int numeroConta = -1;
            int escolha = -1; //usado para diversos menus

            while (true) {
                try {
                    numeroConta = teclado.nextInt();
                    if (numeroConta <= 0) {
                        System.out.println("Número inválido.");
                        teclado.nextLine();
                        continue;
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Número inválido.");
                    teclado.nextLine();
                    continue;
                }
    
                conta = contaDAO.buscarConta(numeroConta);
                if (conta == null) {
                    System.out.println(
                          "Parece que esta conta não existe. Deseja criar uma nova?\n"
                        + "1. Sim\n"
                        + "2. Não"
                    );
    
                    while (true) {
                        try {
                            escolha = teclado.nextInt();
                        } catch (InputMismatchException e) {
                            System.out.println("Escolha inválida.");
                            teclado.nextLine();
                            continue;
                        }
        
                        switch (escolha) {
                            case 1:
                                double limite = 0;
                                
                                System.out.println("Informe o limite da conta (não pode ser alterado!)");

                                while (true) {
                                    System.out.print("R$");

                                    try {
                                        limite = teclado.nextDouble();
                                        if (limite < 0) {
                                            System.out.println("Valor inválido.");
                                            teclado.nextLine();
                                            continue;
                                        }
                                    } catch (InputMismatchException e) {
                                        System.out.println("Valor inválido.");
                                        teclado.nextLine();
                                        continue;
                                    }
                                    break;
                                }

                                conta = contaDAO.novaConta(numeroConta, 0, limite);
                                if (conta == null) {
                                    System.out.println("Erro ao criar conta, tente novamente.");
                                    escolha = 2; //agir como se o usuário tivesse escolhido "Não"
                                    break;
                                }

                                escolha = -1;
                                break;
                            case 2:
                                break;
                            default:
                                System.out.println("Escolha inválida.");
                                continue;
                        }
                        break;
                    }
                }
                break;
            }
            if (escolha == 2) continue;
            break;
        }

        // Menu principal
        while (true) {
            System.out.println(
                  "Escolha uma opção:\n"
                + "1. Exibir saldo\n"
                + "2. Depositar\n"
                + "3. Sacar\n"
                + "4. Transferir\n"
                + "5. Exibir transações\n"
                + "6. Adicionar chave Pix (temp. indisponível)\n"
                + "7. Remover chave Pix (temp. indisponível)\n"
                + "8. Exibir chaves Pix (temp. indisponível)\n"
                + "9. Exibir chaves Pix favoritas (temp. indisponível)\n"
                + "10. Deletar conta\n"
                + "\n"
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
                    case 1: exibirMenu = exibirSaldo(conta);                         break;
                    case 2: exibirMenu = depositar(teclado, conta, contaDAO);        break;
                    case 3: exibirMenu = sacar(teclado, conta, contaDAO);            break;
                    case 4: exibirMenu = transferir(teclado, conta, contaDAO);       break;
                    case 5: exibirMenu = exibirTransacoes(teclado, conta, contaDAO); break;
                    //case 6: exibirMenu = adicionarPix(teclado, conta);             break;
                    //case 7: exibirMenu = removerPix(teclado, conta);               break;
                    //case 8: exibirMenu = exibirPix(conta);                         break;
                    //case 9: exibirMenu = exibirPixFavoritas(conta);                break;
                    case 10: exibirMenu = deletarConta(teclado, conta, contaDAO);    break;
                    case 0:
                        exibirMenu = false;
                        encerrarPrograma = true;
                        break;
                    default:
                        System.out.println("Escolha inválida.");
                        continue;
                }

                // o método transferir já atualiza o saldo
                // e não há razão para atualizar se a conta foi excluída
                if (escolha != 4 && escolha != 10) {
                    contaDAO.atualizarSaldo(conta);
                }

                // fechar tudo se a conta foi excluída
                if (contaDAO.buscarConta(conta.getNumero()) == null) {
                    encerrarPrograma = true;
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
        double saldo = conta.getSaldo();

        System.out.printf(
              "Saldo:  %sR$%.2f%n"
            + "Limite: R$%.2f%n",
              (saldo >= 0) ? "" : "-", Math.abs(saldo),
              conta.getLimite()
        );

        return true;
    }
    
    private static boolean depositar(Scanner teclado, ContaCorrente conta, ContaCorrenteDAO contaDAO) {
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
        
            contaDAO.registrarTransacao("deposit", valor, conta, null);

            System.out.println("Depósito realizado com sucesso!");
            break;
        }

        return true;
    }
    
    private static boolean sacar(Scanner teclado, ContaCorrente conta, ContaCorrenteDAO contaDAO) {
        System.out.print(
              "Informe o valor para saque:\n"
            + "(Digite \"0\" para retornar)\nR$"
        );
        
        double valor = -1;
        
        while (true) {
            try {
                valor = teclado.nextDouble();
                if (valor == 0) return false;
                conta.sacar(valor, false);
            } catch (InputMismatchException e) {
                System.out.print("Valor deve ser um número.\nR$");
                teclado.nextLine();
                continue;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                System.out.print("R$");
                continue;
            }

            contaDAO.registrarTransacao("withdrawal", -valor, conta, null);
        
            System.out.println("Saque realizado com sucesso!");
            break;
        }
        
        return true;
    }
    
    // Eu... não estou super orgulhoso do jeito de eu programei isso
    // Desafio: contar o número de try-catches sem dar Ctrl + F
    private static boolean transferir(Scanner teclado, ContaCorrente conta, ContaCorrenteDAO contaDAO) {
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
                    conta.sacar(valorTransferencia, true); //apenas verificar se há saldo, sem sacar
                } catch (InputMismatchException e) {
                    System.out.print("Valor deve ser um número.\nR$");
                    teclado.nextLine();
                    continue;
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    System.out.print("R$");
                    teclado.nextLine();
                    continue;
                }
                break;
            }
            
            teclado.nextLine(); //limpar o buffer

            while (true) {
                //TODO: implementar chave pix como alternativa
                System.out.println(
                      "Como deseja realizar a transferência?\n"
                    + "1. Número da conta\n"
                    + "2. Chave PIX (temp. indisponível)\n"
                    + "0. Retornar"
                );

                int escolha = -1;
                int numeroDestinatario = -1;

                while (true) {
                    try {
                        escolha = teclado.nextInt();
                    } catch (InputMismatchException e) {
                        System.out.println("Escolha inválida.");
                        teclado.nextLine();
                        continue;
                    }

                    switch (escolha) {
                        case 1:
                            System.out.println(
                                  "Informe o número da conta do destinatário:\n"
                                + "(Digite \"0\" para retornar)"
                            );

                            ContaCorrente contaDestinatario = null;

                            // essencialmente o mesmo código do menu inicial
                            while (true) {
                                try {
                                    numeroDestinatario = teclado.nextInt();
                                    if (numeroDestinatario < 0) {
                                        System.out.println("Número inválido.");
                                        teclado.nextLine();
                                        continue;
                                    }
                                    if (numeroDestinatario == conta.getNumero()) {
                                        System.out.println("Você não pode transferir dinheiro para si mesmo!");
                                        teclado.nextLine();
                                        continue;
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("Número inválido.");
                                    teclado.nextLine();
                                    continue;
                                }

                                if (numeroDestinatario == 0) break;

                                contaDestinatario = contaDAO.buscarConta(numeroDestinatario);

                                if (contaDestinatario == null) {
                                    System.out.println("Conta não encontrada, tente novamente.");
                                    teclado.nextLine();
                                    continue;
                                }

                                double saldo = conta.getSaldo();

                                System.out.printf(
                                      "- Sua conta -%n"
                                    + "Saldo: %sR$%.2f%n"
                                    + "Limite: R$%.2f%n"
                                    + "Valor a ser transferido: R$%.2f%n"
                                    + "%n",
                                    (saldo >= 0) ? "" : "-", Math.abs(saldo),
                                    conta.getLimite(),
                                    valorTransferencia
                                );

                                System.out.println(
                                      "Deseja realizar a transferência?\n"
                                    + "1. Sim\n"
                                    + "2. Não"
                                );

                                while (true) {
                                    try {
                                        escolha = teclado.nextInt();
                                    } catch (InputMismatchException e) {
                                        System.out.println("Escolha inválida.");
                                        teclado.nextLine();
                                        continue;
                                    }

                                    switch (escolha) {
                                        case 1:
                                            System.out.println("Iniciando transferência...");

                                            try {
                                                contaDAO.transferir(valorTransferencia, conta, contaDestinatario);
                                            } catch (Exception e) {
                                                System.out.println("Falha na transferência, tente novamente.");
                                                return true; //return true não significa sucesso, apenas que o menu retornar deve aparecer
                                            }

                                            System.out.println("Transferência realizada com sucesso!");
                                            return true;
                                        case 2:
                                            break;
                                        default:
                                            System.out.println("Escolha inválida.");
                                            continue;
                                    }
                                    break;
                                }
                                break;
                            }
                            if (numeroDestinatario == 0) break;
                            if (escolha == 2) break;
                            continue;
                        //TODO: implementar funcionalidades pix com o banco de dados
                        /*
                        case 2:
                            escolha = -1;

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
                        */
                        case 0:
                            break;
                        default:
                            System.out.println("Escolha inválida.");
                            teclado.nextLine();
                            continue;
                    }
                    break;
                }
                if (numeroDestinatario == 0) continue;
                if (escolha == 2) continue;
                break;
            }
            continue;
        }
    }
    
    private static boolean exibirTransacoes(Scanner teclado, ContaCorrente conta, ContaCorrenteDAO contaDAO) {
        System.out.println(
              "Informe quantas transações deseja exibir:\n"
            + "(Digite \"-1\" para exibir todas)"
        );
        
        int numTransacoes = 0;
        
        while (true) {
            try {
                numTransacoes = teclado.nextInt();

                if (numTransacoes == -1) {
                    contaDAO.exibirTransacoes(conta);
                } else {
                    contaDAO.exibirTransacoes(conta, numTransacoes);
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

        // contaDAO já se encarrega de dar print dos resultados
        
        return true;
    }

    private static boolean deletarConta(Scanner teclado, ContaCorrente conta, ContaCorrenteDAO contaDAO) {
        System.out.println(
              "AVISO: Você está prestes a fechar a sua conta corrente. Esta ação é irreversível!\n"
            + "Tem certeza que deseja permanentemente fechar sua conta?\n"
            + "\n"
            + "1. Sim, desejo FECHAR minha conta\n"
            + "0. Não, desejo MANTER minha conta"
        );
        
        int escolha = -1;
        
        while (true) {
            try {
                escolha = teclado.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Escolha inválida.");
                teclado.nextLine();
                continue;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                continue;
            }

            switch (escolha) {
                case 1:
                    try {
                        contaDAO.deletarConta(conta);
                    } catch (Exception e) {
                        System.out.println("Falha inesperada, tente novamente.");
                        return true;
                    }
                    
                    System.out.println("Conta excluída com sucesso.");
                    return true;
                case 0:
                    return false;
                default:
                    System.out.println("Escolha inválida.");
                    teclado.nextLine();
                    continue;
            }
        }
    }
    
    //TODO: implementar funcionalidades pix com o banco de dados
    /*
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
    */
}