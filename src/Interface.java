//TODO: helper functions para evitar código repetido

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Interface {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Conta Corrente");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        JLabel labelNumConta = new JLabel("Número da conta:");
        JTextField campoNumConta = new JTextField(10);
        JLabel labelValor = new JLabel("Valor: R$");
        JTextField campoValor = new JTextField(10);
        JButton botaoConsulta = new JButton("Consultar saldo");
        //JButton botaoDeposito = new JButton("Realizar depósito");
        JButton botaoSaque = new JButton("Realizar saque");

        botaoConsulta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int numeroConta = 0;
                try {
                    numeroConta = Integer.parseInt(campoNumConta.getText());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Número de conta inválido!");
                    return;
                }

                ContaCorrenteDAO contaDAO = new ContaCorrenteDAO();
                ContaCorrente conta = contaDAO.buscarConta(numeroConta);

                if (conta == null) {
                    JOptionPane.showMessageDialog(null, "Conta não existe!");
                    return;
                }

                JOptionPane.showMessageDialog(null, "Saldo da conta " + numeroConta + ": " + conta.getSaldo());
            }
        });
        botaoSaque.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int numeroConta = 0;
                try {
                    numeroConta = Integer.parseInt(campoNumConta.getText());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Número de conta inválido!");
                    return;
                }

                ContaCorrenteDAO contaDAO = new ContaCorrenteDAO();
                ContaCorrente conta = contaDAO.buscarConta(numeroConta);

                if (conta == null) {
                    JOptionPane.showMessageDialog(null, "Conta não existe!");
                    return;
                }

                double valorSaque = 0;
                try {
                    valorSaque = Double.parseDouble(campoValor.getText());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Valor inválido!");
                    return;
                }

                try {
                    conta.sacar(valorSaque, false);
                    JOptionPane.showMessageDialog(null, "Saque realizado com sucesso!");
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                    return;
                }
                
                contaDAO.atualizarSaldo(conta);
                contaDAO.registrarTransacao("withdrawal", -valorSaque, conta, null);
            }
        });

        panel.add(labelNumConta);
        panel.add(campoNumConta);
        panel.add(labelValor);
        panel.add(campoValor);
        panel.add(botaoConsulta);
        //panel.add(botaoDeposito);
        panel.add(botaoSaque);

        frame.add(panel);
        frame.setVisible(true);
    }
}