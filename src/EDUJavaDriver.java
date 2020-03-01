import java.util.Arrays;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class EDUJavaDriver {
	static String[] portas = null;
	static SerialPort sp = null;
	static byte[] pacoteDeComando = null;
	static byte[] pacoteDeResposta = null;
	static byte[] parametros = null;
	private static byte[] resposta = null; // Pare ser devolvido
	static byte[] statusSensores = null;
	static byte[] versao = null;

	public static int getPortList() throws InterruptedException {
		int rc = SerialPortList.getPortNames().length;
		portas = SerialPortList.getPortNames();
		for (int i = 0; i < rc; i++) {
			System.out.println("drv:getPortList: i[" + i + "] rc[" + rc + "] PortName[" + portas[i] + "]");
		}
		return rc;
	}

	// ===================
	// Metodos Exportaveis
	// ===================
	@SuppressWarnings("static-access")
	public static int abrirComunicacao() throws InterruptedException {
		int rc = -200;
		String NomePorta = "COM4";
		boolean bok;
		if (sp != null) {
			// Ja possui instancia da porta: ela existe
			// System.out.println("drv:abrirComunicacao: Porta[" + NomePorta + "] Ja
			// Existe existe");
			try {
				if (sp.isOpened() == true) { // Porta Ja esta aberta
					rc = lerVersao();
					if (rc >= 0) {
						System.out.println("drv:abrirComunicacao: Porta[" + NomePorta + "] Comunicacao ja estabelecida");
						return rc;
					}
					// Nao comunicou: fechar a porta e prosseguir
					sp.closePort();
				}
			} catch (SerialPortException e) {
				e.printStackTrace();
				System.err.println("drv:abrirComunicacao: rc[" + rc + "] error e[" + e.getMessage() + "]Porta Nao existe");
			}
		}
		try {
			// PASSO 01 - Obter a Instancia da Porta Serial semelhate ao
			// GetCommState/SetCommState
			sp = new SerialPort(NomePorta); // a Instancia chama- se sp
			if (sp == null) {
				// Caso a porta nao exista nao tras a instancia
				System.err.println("drv:abrirComunicacao: Porta[" + NomePorta + "] Nao existe");
				return rc;
			}
			// System.out.println("drv:abrirComunicacao: Porta[" + NomePorta +
			// "]Existe OK");

			// PASSO 02 - Abrir a porta: semelhante ao CreateFile do Windows
			bok = sp.openPort();
			if (bok == false) {
				System.err.println("drv:abrirComunicacao: rc[" + rc + "] Porta[" + NomePorta + "]Nao Abriu");
				return rc;
			}

			// PASSO 03 - Configurar a porta: semelhante ao Get/SetCommstate do
			// Windows
			bok = sp.setParams(sp.BAUDRATE_9600, sp.DATABITS_8, sp.STOPBITS_1, sp.PARITY_NONE);
			if (bok == false) {
				System.err.println("drv:abrirComunicacao: rc[" + rc + "] Porta COM4 Nao Configurada");
				sp.closePort();
				return rc;
			}

			// Levantar DTR
			bok = sp.setDTR(true);
			if (bok == false) {
				System.err.println("drv:abrirComunicacao: rc[" + rc + "] Porta COM4 Nao Ativou DTR");
				sp.closePort();
				return rc;
			}

			// Levantar RTS
			bok = sp.setRTS(true);
			if (bok == false) {
				System.err.println("drv:abrirComunicacao: rc[" + rc + "] Porta COM4 Nao Ativou RTSR");
				sp.closePort();
				return rc;
			}
			// PASS0 04 - Enviar um comando ao Perferico
			rc = lerVersao();
			if (rc < 0) {
				System.err.println("drv:abrirComunicacao: rc[" + rc + "] Nao comunicou com dispositivo");
				sp.closePort();
				return rc;
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
			System.err.println("drv:abrirComunicacao: rc[" + rc + "] error e[" + e.getMessage() + "]Porta Nao existe");
		}
		return rc;
	}

	public static int fecharComunicacao() throws InterruptedException {
		int rc = -201;
		if (sp != null) {
			try {
				if (sp.isOpened() == true) {
					sp.closePort();
					rc = 0;
					System.out.println("drv:fecharComunicacao: rc[" + rc + "] Porta fechada com sucesso");
				} else {
					rc = 1;
					System.out.println("drv:fecharComunicacao: rc[" + rc + "] Porta Ja estava fecjada");
				}
			} catch (SerialPortException e) {
				e.printStackTrace();
				System.out.println("drv:fecharComunicacao: rc[" + rc + "]");
			}
		} else {
			System.out.println("drv:fecharComunicacao: rc[" + rc + "]Porta nao possue INSTANCIA");
		}
		return rc;
	}

	public static int dispensarDocumento() throws InterruptedException {
		int rc = -202;
		parametros = null;
		rc = processarComando((byte) 0x31, parametros, 100);
		if (rc < 0) {
			System.out.println("drv:lerVersao rc[" + rc + "]Valhou");
		} else {
			System.out.println("drv:lerVersao rc[" + rc + "]OK");
		}
		return rc;
	}

	public static int lerStatusSensores() throws InterruptedException {
		int rc = -203;
		parametros = null;
		statusSensores = null;
		rc = processarComando((byte) 0x32, parametros, 100);
		if (rc < 0) {
			System.out.println("drv:lerStatusSensores: rc[" + rc + "]Valhou");
		} else {
			statusSensores = resposta;
			String strStatusSensores = new String(statusSensores);
			System.out.println("drv:lerVersao rc[" + rc + "] strStatusSensores[" + strStatusSensores + "]OK");
		}
		return rc;
	}

	public static int lerVersao() throws InterruptedException {
		int rc = -204;
		parametros = null;
		versao = null;
		rc = processarComando((byte) 0x35, parametros, 100);
		if (rc < 0) {
			System.out.println("drv:lerVersao rc[" + rc + "]Valhou");
		} else {
			versao = resposta;
			String strVersao = new String(versao);
			System.out.println("drv:lerVersao rc[" + rc + "] strVersao[" + strVersao + "]OK");
		}
		return rc;
	}

	//
	// Funcoes locais
	//
	static int processarComando(byte cmd, byte[] parametros, int timeout) {
		int rc = -200;
		int rc1;
		rc = montarPacoteDeComando(cmd, parametros);
		if (rc < 0) {
			System.err.println("drv:processarComando:montarPacoteDeComando rc[" + rc + "]falhou");
			return rc;
		}
		int tentativas = 3;
		while (tentativas > 0) {
			rc = enviarPacoteDeComando(pacoteDeComando);
			if (rc < 0) {
				System.err.println("drv:processarComando:enviarPacoteDeComando rc[" + rc + "]falhou");
				break;
			}
			rc = aguardarConfirmacao(100);
			if (rc == 0) {
				break; // Chegou ACK
			}
			tentativas--;
		}
		if (rc < 0) {
			System.err.println("drv:processarComando:rc[" + rc + "]Falhou tentativas[" + tentativas + "]");
			return rc;
		} else if (tentativas == 0) {
			System.err.println("drv:processarComando:rc[" + rc + "]Falhou tentativas[" + tentativas + "] Esgotadas");
			return rc;
		}
		tentativas = 3;
		while (tentativas > 0) {
			rc = lerPacoteDeResposta(1000);
			if (rc < 0) {
				System.err.println("drv:processarComando:lerPacoteDeResposta rc[" + rc + "]Falhou");
				return rc;
			}
			int tamPacote = rc;
			rc = desmontarPacoteDeResposta(tamPacote);
			if (rc < 0) {
				System.err.println("drv:processarComando:desmontarPacoteDeResposta rc[" + rc + "]falhou");
				break;
			} else if (rc != 9999) {
				rc1 = enviarConfirmacao(true); // enviar ACK
				if (rc1 < 0) {
					System.err.println("drv:processarComando:enviarConfirmacao rc[" + rc1 + "]Falhou");
					return rc1;
				}
				break;
			}
			// LRC invalido
			rc1 = enviarConfirmacao(false); // enviar NAK
			if (rc1 < 0) {
				System.err.println("drv:processarComando:enviarConfirmacao rc[" + rc1 + "]Falhou");
				return rc1;
			}
			tentativas--;
		}
		if (rc < 0) {
			System.err.println("drv:processarComando: rc[" + rc + "]Falhou");
		}
		return rc;
	}

	static int montarPacoteDeComando(byte cmd, byte[] parametros) {
		int rc = -205;
		int i = 0;
		int x;
		byte lrc = 0;
		// STX COM DT1 DT2 <DATA> ETX LRC
		if (parametros != null) {
			pacoteDeComando = new byte[parametros.length + 6];
		} else {
			pacoteDeComando = new byte[6];
		}
		pacoteDeComando[i++] = 0x02; // STX
		pacoteDeComando[i++] = cmd; // COM
		pacoteDeComando[i++] = 0x30; // DT1
		pacoteDeComando[i++] = 0x30; // DT2
		if (parametros != null) {
			for (x = 0; x < parametros.length; x++) {
				pacoteDeComando[i++] = parametros[x];
			}
		}
		pacoteDeComando[i++] = 0x03; // ETX
		// Calcular LRC
		for (x = 0; x < i; x++) {
			lrc ^= pacoteDeComando[x];
		}
		pacoteDeComando[i++] = lrc; // LRC
		rc = i;
		// System.out.println("drv:montarPacoteDeComando: rc[" + rc + "]");
		return rc;
	}

	static int enviarPacoteDeComando(byte[] pacoteDeComando) {
		int rc = -206;
		boolean bok = false;
		if (sp == null) {
			System.out.println("drv:enviarPacoteDeComando: rc[" + rc + "]porta Nao Existe");
			return rc;
		} else if (sp.isOpened() == false) {
			System.out.println("drv:enviarPacoteDeComando: rc[" + rc + "]porta Fechada");
			return rc;
		}
		try {
			System.out.println("drv:enviarPacoteDeComando: >> [" + pacoteDeComando.length + "]Enviando Aguarde....");
			bok = sp.writeBytes(pacoteDeComando);
			System.out.println("drv:enviarPacoteDeComando: >> [" + pacoteDeComando.length + "]Enviado OK");
			if (bok == false) {
				System.out.println("drv:enviarPacoteDeComando: rc[" + rc + "]Nao enviou os dados");
				return rc;
			}
			rc = pacoteDeComando.length;
		} catch (SerialPortException e) {
			e.printStackTrace();
			System.out.println("drv:enviarPacoteDeComando: rc[" + rc + "]writeBytes falhou");
		}
		return rc;
	}

	static int aguardarConfirmacao(int timeout) {
		int rc = -207;
		int iLocalTimeout = 0;
		if (sp == null) {
			System.out.println("drv:aguardarConfirmacao: rc[" + rc + "]porta Nao Existe");
			return rc;
		} else if (sp.isOpened() == false) {
			System.out.println("drv:aguardarConfirmacao: rc[" + rc + "]porta Fechada");
			return rc;
		}
		while (iLocalTimeout < timeout) {
			try {
				byte[] rxbytes = sp.readBytes(1);
				switch (rxbytes[0]) {
				case 0x06: // recebeu ACK
					rc = 0;
					break;
				case 0x15: // recebeu NAK
					rc = 1;
					break;
				default:
					System.out.println("drv:aguardarConfirmacao: recebeu[" + rxbytes[0] + "] esperado ACK ou NAK");
					break;
				}
				if (rc >= 0) {
					break;
				}
				Thread.sleep(1);
				iLocalTimeout++;
			} catch (SerialPortException e) {
				e.printStackTrace();
				System.out.println("drv:aguardarConfirmacao: rc[" + rc + "]falhou");
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("drv:aguardarConfirmacao: rc[" + rc + "]thread.sleep falhou");
			}
		}
		return rc;
	}

	static int lerPacoteDeResposta(int timeout) {
		int rc = -208;
		int iLocalTimeout = 0;
		int i = 0;
		byte[] BufByte = new byte[1];
		pacoteDeResposta = new byte[100];
		if (sp == null) {
			System.out.println("drv:enviarPacoteDeComando: rc[" + rc + "]porta Nao Existe");
			return rc;
		} else if (sp.isOpened() == false) {
			System.out.println("drv:enviarPacoteDeComando: rc[" + rc + "]porta Fechada");
			return rc;
		}
		while (iLocalTimeout < timeout) {
			try {
				BufByte = sp.readBytes(1);
				switch (BufByte[0]) {
				case 0x02: // recebeu STX
					rc = 0;
					break;
				default:
					System.out.println("drv:aguardarConfirmacao: recebeu[" + BufByte[0] + "] esperado STX");
					break;
				}
				if (rc >= 0) {
					break;
				}
				Thread.sleep(1);
				iLocalTimeout++;
			} catch (InterruptedException | SerialPortException e) {
				e.printStackTrace();
				System.out.println("drv:aguardarConfirmacao: rc[" + rc + "]thread.sleep falhou");
			}
		}
		iLocalTimeout = 0;
		pacoteDeResposta[i++] = BufByte[0];
		rc = -222;
		while (iLocalTimeout < timeout) {
			try {
				BufByte = sp.readBytes(1);
				switch (BufByte[0]) {
				case 0x03: // recebeu ETX
					pacoteDeResposta[i++] = BufByte[0]; // ETX
					BufByte = sp.readBytes(1); // Ler LRC
					pacoteDeResposta[i++] = BufByte[0]; // LRC
					rc = 0;
					break;
				default:
					pacoteDeResposta[i++] = BufByte[0]; // PacketData
					break;
				}
				if (rc >= 0) {
					break;
				}
				if (sp.getInputBufferBytesCount() == 0) {
					Thread.sleep(1);
					iLocalTimeout++;
				}
			} catch (InterruptedException | SerialPortException e) {
				e.printStackTrace();
				System.out.println("drv:aguardarConfirmacao: rc[" + rc + "]thread.sleep falhou");
			}
		}
		if (rc >= 0) {
			rc = i;
		}
		return rc;
	}

	static int desmontarPacoteDeResposta(int tamPacote) {
		int rc = -209;
		int i;
		byte lrc = 0;
		if (tamPacote <= 0) {
			System.out.println("drv:desmontarPacoteDeResposta: rc[" + rc + "]tamPacote[" + tamPacote + "]invalido");
		} else if (pacoteDeResposta == null) {
			System.out.println(
					"desmontarPacoteDeResposta: rc[" + rc + "]tamPacote[" + tamPacote + "]pacoteDeResposta=null");
		} else if (pacoteDeResposta[0] != 0x02) {
			System.out
					.println("desmontarPacoteDeResposta: rc[" + rc + "]tamPacote[" + tamPacote + "]Nao comeca com STX");
		} else if (pacoteDeResposta[tamPacote - 2] != 0x03) {
			System.out.println(
					"desmontarPacoteDeResposta: rc[" + rc + "]tamPacote[" + tamPacote + "]Nao Ternuba com ETX LRC");
		}
		for (i = 0; i < tamPacote - 1; i++) {
			lrc ^= pacoteDeResposta[i];
		}
		if (lrc != pacoteDeResposta[tamPacote - 1]) {
			// LRC Invalido
			rc = 9999;
			System.out.println("drv:desmontarPacoteDeResposta: rc[" + rc + "]tamPacote[" + tamPacote + "] LRC[" + lrc
					+ "]Rec[" + pacoteDeResposta[tamPacote - 1] + "]FAIL");
			return rc;
		} else {
			// LRC Valido devolver resposta OK
			rc = tamPacote - 4; // STX COM <DATA> ETX LRC
			if (rc > 0) {
				resposta = new byte[rc];
				// Copia Resposta de pacoteDeresposta
				resposta = Arrays.copyOfRange(pacoteDeResposta, 2, (tamPacote - 2));
			} else {
				resposta = null;
			}
		}
		return rc;
	}

	static int enviarConfirmacao(boolean back) {
		int rc = -200;
		if (sp == null) {
			System.err.println("drv:enviarPacoteDeComando: rc[" + rc + "]porta Nao Existe");
			return rc;
		} else if (sp.isOpened() == false) {
			System.err.println("drv:enviarPacoteDeComando: rc[" + rc + "]porta Fechada");
			return rc;
		}
		try {
			if (back == true) {
				sp.writeByte((byte) 0x06);
				rc = 1;
			} else {
				sp.writeByte((byte) 0x15);
				rc = 2;
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
			System.err.println("drv:enviarPacoteDeComando: rc[" + rc + "]falhou");
		}
		return rc;
	}

	public static byte[] getResposta() {
		return resposta;
	}
}
