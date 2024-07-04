
	/////////////////// debut lettrage
	/////////////////// /////////////////////////////////////////////////////////////////////////////

	public void lettrage(EventEngineRules eventEngineRule, Company cmp) throws ParseException, DocumentException {

		// List<String> accountList = accountService
		// .loadAccountCodeByStatusFiltreWithoutInit(StatusRecord.Authorized.toString(),
		// cmp.getCompanyId());

		PaginationCriteria pagination2 = new PaginationCriteria();

		pagination2.setFilter(eventEngineRule.getRuleQuery());
		int positionNum = 1 ;
		
		Date dateLettrage = new Date() ;

		// pagination2.setFilter3("( recStatus is null or recStatus = 0 )");

		List<String> accountList = journalEntryServiceImpl.loadAccountsFormJE(cmp, pagination2, null);

		List<JournalEntry> listToUpdate = new ArrayList<>();

		List<ArrayList<String>> valueColumns = new ArrayList<ArrayList<String>>();

		List<String> labelColumns = new ArrayList<String>();

		labelColumns.add("Booking Date");
		labelColumns.add("Batch Ref");

		labelColumns.add("Contract ID");
		labelColumns.add("Line ID");
		labelColumns.add("Tr Code");
		labelColumns.add("Product");
		labelColumns.add("Account Code");
		labelColumns.add("narrative1");
		labelColumns.add("Mvt.LCY");
		labelColumns.add("Mvt.FCY");
		labelColumns.add("Currency");
		labelColumns.add("Entry ID");
		labelColumns.add("Reconciliation Code");
		labelColumns.add("Reconciliation Type");
		labelColumns.add("Numero");

		// last commented by rah
//		labelColumns.add("Entry ID");
//		labelColumns.add("Reconciliation Code");
//		labelColumns.add("Booking Date");
//		labelColumns.add("Batch Ref");
//		labelColumns.add("Contract ID");
		//	labelColumns.add("Numero");
//		labelColumns.add("Line ID");
//		labelColumns.add("Tr Code");
//		labelColumns.add("Product");
//		labelColumns.add("Account Code");
//		labelColumns.add("narrative1");
//		labelColumns.add("Mvt.LCY");
//		labelColumns.add("Mvt.FCY");
//		labelColumns.add("Currency");
		// last commented by rah
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		boolean recTYPE = false;

		boolean datebooking = false;
		boolean batchref = false;
		boolean contractID = false;

		boolean contractIDEquals = false;

		boolean contractIDSeq = false;

		boolean product = false;
		boolean narrative1 = false;
		// boolean reconStatus = false ;
		String reconcType = "";

		if (eventEngineRule.getEventRuleLines().get(0).getMultipleLine() != null) {
			recTYPE = eventEngineRule.getEventRuleLines().get(0).getMultipleLine();
		}
		if (eventEngineRule.getEventRuleLines().get(0).getBookingDate() != null) {
			datebooking = eventEngineRule.getEventRuleLines().get(0).getBookingDate();
		}
		if (eventEngineRule.getEventRuleLines().get(0).getBatchRef() != null) {
			batchref = eventEngineRule.getEventRuleLines().get(0).getBatchRef();
		}
		if (eventEngineRule.getEventRuleLines().get(0).getContractId() != null) {
			contractID = eventEngineRule.getEventRuleLines().get(0).getContractId();
		}
		if (eventEngineRule.getEventRuleLines().get(0).getProduct() != null) {
			product = eventEngineRule.getEventRuleLines().get(0).getProduct();
		}

		if (eventEngineRule.getEventRuleLines().get(0).getNarrative1() != null) {
			narrative1 = eventEngineRule.getEventRuleLines().get(0).getNarrative1();
		}

		if (!recTYPE) {
			reconcType = "Line by Line";
		} else {
			reconcType = "Multiple Line ";
		}

		// if (datebooking || batchref || contractID || product || narrative1 )
		// {
		for (String accountCode : accountList) {

			List<JournalEntry> journalEntriesDEBIT = new ArrayList<JournalEntry>();
			List<JournalEntry> journalEntriesCREDIT = new ArrayList<JournalEntry>();

			List<JournalEntry> journalEntriesSM = new ArrayList<JournalEntry>();
			List<JournalEntry> journalEntriesBG = new ArrayList<JournalEntry>();

			// List<JournalEntry> journalEntries =
			// journalServiceImpl.loadNotReconciliatedALLJournalEntries("GL",
			// cmp.getCompanyId());

			String dateD = "2020-01-01";
			Date dateDebut = new SimpleDateFormat("yyyy-MM-dd").parse(dateD);
			String dateF = "2020-06-30";
			Date dateFin = new SimpleDateFormat("yyyy-MM-dd").parse(dateF);

			// List<JournalEntry> journalEntries =
			// journalServiceImpl.loadNotReconciliatedJournalEntriesByAccountCodeAndPeriod(accountCode,
			// dateDebut, dateFin, cmp.getCompanyId());
			
			
//			/// annuler 
//			List<JournalEntry> journalEntries = journalServiceImpl
//					.loadNotReconciliatedJournalEntriesByAccountCodeAndFilter(accountCode, eventEngineRule,
//							cmp.getCompanyId());
//			// annuler 
			
			List<JournalEntry> journalEntries = journalServiceImpl
					.loadNotReconciliatedJournalEntriesByAccountCode(accountCode, 
							cmp.getCompanyId());
			
			

			// last add by rah
			if (!journalEntries.isEmpty()) {

				for (JournalEntry jrNonRecNull : journalEntries) {

					if (jrNonRecNull.getRecStatus() == null) {
						jrNonRecNull.setRecStatus(false);
					}

				}
			}

			// last add by rah

			if (datebooking) {
				journalEntries = journalEntries.stream().filter(x -> x.getBookDate() != null)
						.collect(Collectors.toList());
			}
			// if (batchref) {
			// journalEntries = journalEntries.stream().filter(x ->
			// x.getDocNumber() != null)
			// .collect(Collectors.toList());
			// }

			if (batchref) {
				journalEntries = journalEntries.stream().filter(x -> x.getTransactionReference() != null)
						.collect(Collectors.toList());
			}
			if (contractID) {
				journalEntries = journalEntries.stream().filter(x -> x.getContractId() != null)
						.collect(Collectors.toList());
			}
			if (product) {
				journalEntries = journalEntries.stream().filter(x -> x.getProductCategory() != null)
						.collect(Collectors.toList());
			}
			if (narrative1) {
				journalEntries = journalEntries.stream().filter(x -> x.getTransactionDescription() != null)
						.collect(Collectors.toList());
			}

			if (!journalEntries.isEmpty()) {
				
				for (JournalEntry jrE : journalEntries) {
					if ((jrE.getAmount() != null && jrE.getAmount() < 0)) {
						journalEntriesDEBIT.add(jrE);
					}

					if ((jrE.getAmount() != null && jrE.getAmount() > 0)) {
						journalEntriesCREDIT.add(jrE);
					}
				}

				if (!journalEntriesDEBIT.isEmpty() && !journalEntriesCREDIT.isEmpty()) {

					if (journalEntriesDEBIT.size() < journalEntriesCREDIT.size()) {
						journalEntriesSM.addAll(journalEntriesDEBIT);
						journalEntriesBG.addAll(journalEntriesCREDIT);

					} else if (journalEntriesDEBIT.size() > journalEntriesCREDIT.size()) {
						journalEntriesSM.addAll(journalEntriesCREDIT);
						journalEntriesBG.addAll(journalEntriesDEBIT);

					} else if (journalEntriesDEBIT.size() == journalEntriesCREDIT.size()) {
						journalEntriesSM.addAll(journalEntriesDEBIT);
						journalEntriesBG.addAll(journalEntriesCREDIT);

					}

					if (!recTYPE) {
						// bdina beha
						for (JournalEntry jrSM : journalEntriesSM) {

							List<JournalEntry> jrFiltree = new ArrayList<JournalEntry>();
							positionNum++;
							// Ligne ---> ligne
							// if (!recTYPE) {

							if (datebooking) {
								jrFiltree = journalEntriesBG.stream()
										.filter(x -> x.getBookDate().equals(jrSM.getBookDate()))
										.collect(Collectors.toList());
							} else {
								jrFiltree = journalEntriesBG.stream().collect(Collectors.toList());

							}

							// if (batchref) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getDocNumber().equals(jrSM.getDocNumber()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }

							if (batchref) {
								jrFiltree = jrFiltree.stream()
										.filter(x -> x.getTransactionReference().equals(jrSM.getTransactionReference()))
										.collect(Collectors.toList());
							} else {
								jrFiltree = jrFiltree.stream().collect(Collectors.toList());

							}

							if (contractID) {
								jrFiltree = jrFiltree.stream()
										.filter(x -> x.getContractId().equals(jrSM.getContractId()))
										.collect(Collectors.toList());
							} else {
								jrFiltree = jrFiltree.stream().collect(Collectors.toList());

							}

							if (product) {
								jrFiltree = jrFiltree.stream()
										.filter(x -> x.getProductCategory().equals(jrSM.getProductCategory()))
										.collect(Collectors.toList());
							} else {
								jrFiltree = jrFiltree.stream().collect(Collectors.toList());

							}

							if (narrative1) {
								jrFiltree = jrFiltree.stream().filter(
										x -> x.getTransactionDescription().equals(jrSM.getTransactionDescription()))
										.collect(Collectors.toList());
							} else {
								jrFiltree = jrFiltree.stream().collect(Collectors.toList());

							}

							// if (contractID) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getContractId().replaceAll("[^0-9]",
							// "").equals(jrSM.getContractId().replaceAll("[^0-9]",
							// "")))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }
							//
							// if (contractID) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getContractId().replaceAll("[^0-9]",
							// "").equals(String.valueOf(
							// (Integer.valueOf(jrSM.getContractId().replaceAll("[^0-9]",
							// ""))-1) )))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }

							//

							// modification pour ignorer lka3ba journal entry
							// eli deja reconcili�
							if (!jrFiltree.isEmpty()) {
								
				String referenceJE = sdf.format(dateLettrage) ;

								for (JournalEntry jrF : jrFiltree) {

									Boolean reconStatus = jrF.getRecStatus();
									// bech narj3ou lahne
									if (Math.abs(jrSM.getAmount()) == Math.abs(jrF.getAmount())
											&& (reconStatus == false)) {

										// jrSM.setReconciliationCode(jrF.getjECode());
										
										// modification le 29-11-2022
									
										int cpt = 1;
										//jrSM.setReconciliationCode(jrSM.getjECode());
										jrSM.setReconciliationCode(referenceJE + "." +eventEngineRule.getEventRulesCode()+"."+ String.valueOf(positionNum) + "." + String.valueOf(cpt));
										jrSM.setRecStatus(true);

										listToUpdate.add(jrSM);
										// journalEntryServiceImpl.updateJournalEntry(jrSM);

									//	jrF.setReconciliationCode(jrSM.getjECode());
										jrF.setReconciliationCode(referenceJE + "." 
									+eventEngineRule.getEventRulesCode()+"."+ String.valueOf(positionNum) + "." + String.valueOf(cpt));
										jrF.setRecStatus(true);
										listToUpdate.add(jrF);
										// journalEntryServiceImpl.updateJournalEntry(jrF);

										ArrayList<String> aList = new ArrayList<String>();

										aList.add(sdf.format(jrSM.getBookDate()));
										// aList.add(jrSM.getDocNumber());
										aList.add(jrSM.getTransactionReference());

										aList.add(jrSM.getContractId());
										aList.add(jrSM.getLineNumber());
										aList.add(jrSM.getTransactionCode());
										aList.add(jrSM.getProductCategory());
										aList.add(jrSM.getAccountCode());

										aList.add(jrSM.getTransactionDescription());
										aList.add(customFormtFloat("#,###.###", jrSM.getAmount(), "", '.', 3,
												cmp.getFunctionCurrency().getCurrencyCode()));

										aList.add(customFormtFloat("#,###.###", jrSM.getForeignCurrencyAmount(), "",
												'.', 3, cmp.getFunctionCurrency().getCurrencyCode()));
										aList.add(jrSM.getCurrency());
										aList.add(jrSM.getjECode());
										aList.add(jrSM.getReconciliationCode());
										aList.add("Line/Line");
										aList.add("1");

										aList.add(jrF.getjECode());
										aList.add(jrF.getReconciliationCode());
										aList.add(sdf.format(jrF.getBookDate()));
										// aList.add(jrF.getDocNumber());
										aList.add(jrF.getTransactionReference());

										aList.add(jrF.getContractId());
										aList.add(jrF.getLineNumber());
										aList.add(jrF.getTransactionCode());
										aList.add(jrF.getProductCategory());
										aList.add(jrF.getAccountCode());
										aList.add(jrF.getTransactionDescription());
										aList.add(customFormtFloat("#,###.###", jrF.getAmount(), "", '.', 3,
												cmp.getFunctionCurrency().getCurrencyCode()));

										aList.add(customFormtFloat("#,###.###", jrF.getForeignCurrencyAmount(), "", '.',
												3, cmp.getFunctionCurrency().getCurrencyCode()));
										aList.add(jrF.getCurrency());

										valueColumns.add(aList);

										journalEntriesBG.remove(jrF);

										break;

									}
								}
							}

							// end rectype 1 }

							/// ligne ---> multiLigne
							// else {
							//
							// // if
							// (jrSM.getContractId().equalsIgnoreCase("DXTRA1907975085"))
							// {
							// // System.out.println(jrSM.getContractId());
							// // }
							//
							// if (datebooking) {
							// jrFiltree = journalEntriesBG.stream()
							// .filter(x ->
							// x.getBookDate().equals(jrSM.getBookDate()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// journalEntriesBG.stream().collect(Collectors.toList());
							//
							// }
							//
							// if (batchref) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getDocNumber().equals(jrSM.getDocNumber()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }
							//
							// if (contractID) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getContractId().equals(jrSM.getContractId()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }
							//
							// if (product) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getProductCategory().equals(jrSM.getProductCategory()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }
							//
							// if (narrative1) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getTransactionDescription().equals(jrSM.getTransactionDescription()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }
							//
							// ////////////////////////////////////////////////
							// double Somme = 0;
							// double Reste = 0;
							//
							// List<JournalEntry> jrAux = new
							// ArrayList<JournalEntry>();
							// List<JournalEntry> jrNDHIFA = new
							// ArrayList<JournalEntry>();
							//
							// if (!jrFiltree.isEmpty()) {
							//
							// for (JournalEntry jrFr : jrFiltree) {
							//
							// if ((jrFr.getAmount() != null)
							//
							// //na7ina l'egalit�
							// && (jrFr.getAmount() <=
							// Math.abs(jrSM.getAmount()))) {
							//
							// jrAux.add(jrFr);
							// }
							// }
							//
							// Collections.sort(jrAux,
							// (JournalEntry a, JournalEntry b) ->
							// a.getAmount().compareTo(b.getAmount()));
							//
							// Collections.reverse(jrAux);
							//
							// for (JournalEntry jrAuxx : jrAux) {
							//
							// Reste = (Math.abs(jrSM.getAmount()) -
							// jrAuxx.getAmount());
							//
							// jrNDHIFA.add(jrAuxx);
							// Somme += jrAuxx.getAmount();
							// jrAux.removeAll(jrNDHIFA);
							//
							// break;
							// }
							//
							// if (!jrAux.isEmpty()) {
							//
							// for (JournalEntry jrAuxxx : jrAux) {
							//
							// if (Reste - jrAuxxx.getAmount() >= 0) {
							// jrNDHIFA.add(jrAuxxx);
							// Reste -= jrAuxxx.getAmount();
							// Somme += jrAuxxx.getAmount();
							//
							// }
							// }
							// }
							//
							// if (Math.abs(jrSM.getAmount()) ==
							// Math.abs(Somme)) {
							//
							// jrSM.setReconciliationCode(jrSM.getjECode());
							// jrSM.setRecStatus(true);
							// //
							// journalEntryServiceImpl.updateJournalEntry(jrSM);
							// listToUpdate.add(jrSM);
							//
							// int cmpt = 0;
							// int cmpt2 = 0;
							//
							// for (JournalEntry journalEntry : jrNDHIFA) {
							// journalEntry.setReconciliationCode(jrSM.getjECode());
							// journalEntry.setRecStatus(true);
							// //
							// journalEntryServiceImpl.updateJournalEntry(journalEntry);
							// listToUpdate.add(journalEntry);
							//
							// ArrayList<String> aList = new
							// ArrayList<String>();
							//
							// if (cmpt == 0) {
							//
							// cmpt++;
							// cmpt2++;
							//
							// aList.add(sdf.format(jrSM.getBookDate()));
							// aList.add(jrSM.getDocNumber());
							//
							// aList.add(jrSM.getContractId());
							// aList.add(jrSM.getLineNumber());
							// aList.add(jrSM.getTransactionCode());
							// aList.add(jrSM.getProductCategory());
							// aList.add(jrSM.getTransactionDescription());
							// aList.add(customFormtFloat("#,###.###",
							// jrSM.getAmount(), "", '.', 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							//
							// aList.add(customFormtFloat("#,###.###",
							// jrSM.getForeignCurrencyAmount(),
							// "", '.', 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							// aList.add(jrSM.getCurrency());
							// aList.add(jrSM.getjECode());
							// aList.add(jrSM.getReconciliationCode());
							//
							// if (jrNDHIFA.size() > 1) {
							// aList.add("Multiple Line");
							// } else {
							// aList.add("Line/Line");
							// }
							// aList.add(String.valueOf(cmpt));
							// aList.add(journalEntry.getjECode());
							// aList.add(journalEntry.getReconciliationCode());
							// aList.add(sdf.format(journalEntry.getBookDate()));
							// aList.add(journalEntry.getDocNumber());
							//
							// aList.add(journalEntry.getContractId());
							// aList.add(journalEntry.getLineNumber());
							// aList.add(journalEntry.getTransactionCode());
							// aList.add(journalEntry.getProductCategory());
							// aList.add(jrSM.getTransactionDescription());
							// aList.add(customFormtFloat("#,###.###",
							// journalEntry.getAmount(), "",
							// '.', 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							//
							// aList.add(customFormtFloat("#,###.###",
							// journalEntry.getForeignCurrencyAmount(), "", '.',
							// 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							// aList.add(journalEntry.getCurrency());
							//
							// } else {
							// cmpt2++;
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add(String.valueOf(cmpt2));
							// aList.add(journalEntry.getjECode());
							// aList.add(journalEntry.getReconciliationCode());
							// aList.add(sdf.format(journalEntry.getBookDate()));
							// aList.add(journalEntry.getDocNumber());
							//
							// aList.add(journalEntry.getContractId());
							// aList.add(journalEntry.getLineNumber());
							// aList.add(journalEntry.getTransactionCode());
							// aList.add(journalEntry.getProductCategory());
							// aList.add(journalEntry.getTransactionDescription());
							// aList.add(customFormtFloat("#,###.###",
							// journalEntry.getAmount(), "",
							// '.', 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							//
							// aList.add(customFormtFloat("#,###.###",
							// journalEntry.getForeignCurrencyAmount(), "", '.',
							// 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							// aList.add(journalEntry.getCurrency());
							//
							// }
							//
							// valueColumns.add(aList);
							// }
							//
							// journalEntriesBG.removeAll(jrNDHIFA);
							//
							// }
							//
							// }
							// }
						}

						for (JournalEntry jrSM : journalEntriesBG) {

							List<JournalEntry> jrFiltree = new ArrayList<JournalEntry>();
							positionNum++;
							// Ligne ---> ligne
							// if (!recTYPE) {

							if (datebooking) {
								jrFiltree = journalEntriesSM.stream()
										.filter(x -> x.getBookDate().equals(jrSM.getBookDate()))
										.collect(Collectors.toList());
							} else {
								jrFiltree = journalEntriesSM.stream().collect(Collectors.toList());

							}

							// if (batchref) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getDocNumber().equals(jrSM.getDocNumber()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }

							if (batchref) {
								jrFiltree = jrFiltree.stream()
										.filter(x -> x.getTransactionReference().equals(jrSM.getTransactionReference()))
										.collect(Collectors.toList());
							} else {
								jrFiltree = jrFiltree.stream().collect(Collectors.toList());

							}

							if (contractID) {
								jrFiltree = jrFiltree.stream()
										.filter(x -> x.getContractId().equals(jrSM.getContractId()))
										.collect(Collectors.toList());
							} else {
								jrFiltree = jrFiltree.stream().collect(Collectors.toList());

							}

							if (product) {
								jrFiltree = jrFiltree.stream()
										.filter(x -> x.getProductCategory().equals(jrSM.getProductCategory()))
										.collect(Collectors.toList());
							} else {
								jrFiltree = jrFiltree.stream().collect(Collectors.toList());

							}

							if (narrative1) {
								jrFiltree = jrFiltree.stream().filter(
										x -> x.getTransactionDescription().equals(jrSM.getTransactionDescription()))
										.collect(Collectors.toList());
							} else {
								jrFiltree = jrFiltree.stream().collect(Collectors.toList());

							}

							//
							if (!jrFiltree.isEmpty()) {
								
								String referenceJE = String.valueOf(com.uniqweb.utils.DateUtils.dateToJulian(Calendar.getInstance()))
										.replace(".", "");

								for (JournalEntry jrF : jrFiltree) {

									Boolean reconStatus = jrF.getRecStatus();

									if (Math.abs(jrSM.getAmount()) == Math.abs(jrF.getAmount())
											&& (reconStatus == false)) {
										
										
										// modification le 29-11-2022
										
										int cpt = 1;
										//jrSM.setReconciliationCode(jrSM.getjECode());
										jrSM.setReconciliationCode(referenceJE + "."
										+eventEngineRule.getEventRulesCode()+"."+ String.valueOf(positionNum) + "." + String.valueOf(cpt));

										//jrSM.setReconciliationCode(jrF.getjECode());
										jrSM.setRecStatus(true);

										listToUpdate.add(jrSM);
										// journalEntryServiceImpl.updateJournalEntry(jrSM);

										//jrF.setReconciliationCode(jrSM.getjECode());
										jrF.setReconciliationCode(referenceJE + "."
											+eventEngineRule.getEventRulesCode()+"."+ String.valueOf(positionNum) + "." + String.valueOf(cpt));

										jrF.setRecStatus(true);
										listToUpdate.add(jrF);
										// journalEntryServiceImpl.updateJournalEntry(jrF);

										ArrayList<String> aList = new ArrayList<String>();

										aList.add(sdf.format(jrSM.getBookDate()));
										// aList.add(jrSM.getDocNumber());
										aList.add(jrSM.getTransactionReference());

										aList.add(jrSM.getContractId());
										aList.add(jrSM.getLineNumber());
										aList.add(jrSM.getTransactionCode());
										aList.add(jrSM.getProductCategory());
										aList.add(jrSM.getAccountCode());
										aList.add(jrSM.getTransactionDescription());
										aList.add(customFormtFloat("#,###.###", jrSM.getAmount(), "", '.', 3,
												cmp.getFunctionCurrency().getCurrencyCode()));

										aList.add(customFormtFloat("#,###.###", jrSM.getForeignCurrencyAmount(), "",
												'.', 3, cmp.getFunctionCurrency().getCurrencyCode()));
										aList.add(jrSM.getCurrency());
										aList.add(jrSM.getjECode());
										aList.add(jrSM.getReconciliationCode());
										aList.add("Line/Line");
										aList.add(String.valueOf("1"));
										aList.add(jrF.getjECode());
										aList.add(jrF.getReconciliationCode());
										aList.add(sdf.format(jrF.getBookDate()));
										// aList.add(jrF.getDocNumber());
										aList.add(jrF.getTransactionReference());

										aList.add(jrF.getContractId());
										aList.add(jrF.getLineNumber());
										aList.add(jrF.getTransactionCode());
										aList.add(jrF.getProductCategory());
										aList.add(jrF.getAccountCode());
										aList.add(jrF.getTransactionDescription());
										aList.add(customFormtFloat("#,###.###", jrF.getAmount(), "", '.', 3,
												cmp.getFunctionCurrency().getCurrencyCode()));

										aList.add(customFormtFloat("#,###.###", jrF.getForeignCurrencyAmount(), "", '.',
												3, cmp.getFunctionCurrency().getCurrencyCode()));
										aList.add(jrF.getCurrency());

										valueColumns.add(aList);

										journalEntriesSM.remove(jrF);

										break;

									}
								}
							}

							// end rectype}

							/// ligne ---> multiLigne
							// else {
							//
							// // if
							// (jrSM.getContractId().equalsIgnoreCase("DXTRA1907975085"))
							// {
							// // System.out.println(jrSM.getContractId());
							// // }
							//
							// if (datebooking) {
							// jrFiltree = journalEntriesSM.stream()
							// .filter(x ->
							// x.getBookDate().equals(jrSM.getBookDate()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// journalEntriesSM.stream().collect(Collectors.toList());
							//
							// }
							//
							// if (batchref) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getDocNumber().equals(jrSM.getDocNumber()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }
							//
							// if (contractID) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getContractId().equals(jrSM.getContractId()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }
							//
							// if (product) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getProductCategory().equals(jrSM.getProductCategory()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }
							//
							// if (narrative1) {
							// jrFiltree = jrFiltree.stream()
							// .filter(x ->
							// x.getTransactionDescription().equals(jrSM.getTransactionDescription()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltree =
							// jrFiltree.stream().collect(Collectors.toList());
							//
							// }
							//
							// ////////////////////////////////////////////////
							// double Somme = 0;
							// double Reste = 0;
							//
							// List<JournalEntry> jrAux = new
							// ArrayList<JournalEntry>();
							// List<JournalEntry> jrNDHIFA = new
							// ArrayList<JournalEntry>();
							//
							// if (!jrFiltree.isEmpty()) {
							//
							// for (JournalEntry jrFr : jrFiltree) {
							//
							// if ((jrFr.getAmount() != null)
							//
							// //na7ina l'egalit� une autre fois nope after test
							// && (jrFr.getAmount() <=
							// Math.abs(jrSM.getAmount()))) {
							//
							// jrAux.add(jrFr);
							// }
							// }
							//
							// Collections.sort(jrAux,
							// (JournalEntry a, JournalEntry b) ->
							// a.getAmount().compareTo(b.getAmount()));
							//
							// Collections.reverse(jrAux);
							//
							// for (JournalEntry jrAuxx : jrAux) {
							//
							// Reste = (Math.abs(jrSM.getAmount()) -
							// jrAuxx.getAmount());
							//
							// jrNDHIFA.add(jrAuxx);
							// Somme += jrAuxx.getAmount();
							// jrAux.removeAll(jrNDHIFA);
							//
							// break;
							// }
							//
							// if (!jrAux.isEmpty()) {
							//
							// for (JournalEntry jrAuxxx : jrAux) {
							//
							// if (Reste - jrAuxxx.getAmount() >= 0) {
							// jrNDHIFA.add(jrAuxxx);
							// Reste -= jrAuxxx.getAmount();
							// Somme += jrAuxxx.getAmount();
							//
							// }
							// }
							// }
							//
							// if (Math.abs(jrSM.getAmount()) ==
							// Math.abs(Somme)) {
							//
							// jrSM.setReconciliationCode(jrSM.getjECode());
							// jrSM.setRecStatus(true);
							// //
							// journalEntryServiceImpl.updateJournalEntry(jrSM);
							// listToUpdate.add(jrSM);
							// int ct = 0;
							// int ct2 = 0;
							//
							// for (JournalEntry journalEntry : jrNDHIFA) {
							// journalEntry.setReconciliationCode(jrSM.getjECode());
							// journalEntry.setRecStatus(true);
							// //
							// journalEntryServiceImpl.updateJournalEntry(journalEntry);
							// listToUpdate.add(journalEntry);
							//
							// ArrayList<String> aList = new
							// ArrayList<String>();
							//
							// if (ct == 0) {
							//
							// ct++;
							// ct2++;
							//
							// aList.add(sdf.format(jrSM.getBookDate()));
							// aList.add(jrSM.getDocNumber());
							//
							// aList.add(jrSM.getContractId());
							// aList.add(jrSM.getLineNumber());
							// aList.add(jrSM.getTransactionCode());
							// aList.add(jrSM.getProductCategory());
							// aList.add(jrSM.getTransactionDescription());
							// aList.add(customFormtFloat("#,###.###",
							// jrSM.getAmount(), "", '.', 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							//
							// aList.add(customFormtFloat("#,###.###",
							// jrSM.getForeignCurrencyAmount(),
							// "", '.', 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							// aList.add(jrSM.getCurrency());
							// aList.add(jrSM.getjECode());
							// aList.add(jrSM.getReconciliationCode());
							//
							// if (jrNDHIFA.size() > 1) {
							// aList.add("Multiple Line");
							// } else {
							// aList.add("Line/Line");
							// }
							//
							// aList.add(String.valueOf(ct2));
							// aList.add(journalEntry.getjECode());
							// aList.add(journalEntry.getReconciliationCode());
							// aList.add(sdf.format(journalEntry.getBookDate()));
							// aList.add(journalEntry.getDocNumber());
							//
							// aList.add(journalEntry.getContractId());
							// aList.add(journalEntry.getLineNumber());
							// aList.add(journalEntry.getTransactionCode());
							// aList.add(journalEntry.getProductCategory());
							// aList.add(journalEntry.getTransactionDescription());
							// aList.add(customFormtFloat("#,###.###",
							// journalEntry.getAmount(), "",
							// '.', 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							//
							// aList.add(customFormtFloat("#,###.###",
							// journalEntry.getForeignCurrencyAmount(), "", '.',
							// 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							// aList.add(journalEntry.getCurrency());
							//
							// ct++;
							// } else {
							// ct2++;
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add("");
							// aList.add(String.valueOf(ct2));
							// aList.add(journalEntry.getjECode());
							// aList.add(journalEntry.getReconciliationCode());
							// aList.add(sdf.format(journalEntry.getBookDate()));
							// aList.add(journalEntry.getDocNumber());
							//
							// aList.add(journalEntry.getContractId());
							// aList.add(journalEntry.getLineNumber());
							// aList.add(journalEntry.getTransactionCode());
							// aList.add(journalEntry.getProductCategory());
							// aList.add(journalEntry.getTransactionDescription());
							// aList.add(customFormtFloat("#,###.###",
							// journalEntry.getAmount(), "",
							// '.', 3,
							// cmp.getFunctionCurrency().getCurrencyCode()));
							//
							// aList.add(customFormtFloat("#,###.###",
							// cmp.getFunctionCurrency().getCurrencyCode()));
							// aList.add(journalEntry.getCurrency());
							//
							// }
							//
							// valueColumns.add(aList);
							// }
							//
							// journalEntriesSM.removeAll(jrNDHIFA);
							//
							// }
							//
							// }
							// }
						}

					} // end rectype ligne par ligne

					else { // multiple line

						for (JournalEntry jrSM : journalEntriesSM) {
							List<JournalEntry> jrFiltreeCredit = new ArrayList<JournalEntry>();
							List<JournalEntry> jrFiltreeDebit = new ArrayList<JournalEntry>();
							positionNum++;
							// filtrer les debit
							double sommeCredit = 0;
							double sommeDebit = 0;
							double sommeCreditArrondi = 0;
							double sommeDebitArrondi = 0;

							if (datebooking) {
								jrFiltreeDebit = journalEntriesSM.stream()
										.filter(x -> x.getBookDate().equals(jrSM.getBookDate()))
										.collect(Collectors.toList());
							} else {
								jrFiltreeDebit = journalEntriesSM.stream().collect(Collectors.toList());

							}

							// if (batchref) {
							// jrFiltreeDebit = jrFiltreeDebit.stream()
							// .filter(x ->
							// x.getDocNumber().equals(jrSM.getDocNumber()))
							// .collect(Collectors.toList());
							// } else {
							// jrFiltreeDebit =
							// jrFiltreeDebit.stream().collect(Collectors.toList());
							//
							// }

							if (batchref) {
								jrFiltreeDebit = jrFiltreeDebit.stream()
										.filter(x -> x.getTransactionReference().equals(jrSM.getTransactionReference()))
										.collect(Collectors.toList());
							} else {
								jrFiltreeDebit = jrFiltreeDebit.stream().collect(Collectors.toList());

							}

							if (contractID) {
								jrFiltreeDebit = jrFiltreeDebit.stream()
										.filter(x -> x.getContractId().equals(jrSM.getContractId()))
										.collect(Collectors.toList());
							} else {
								jrFiltreeDebit = jrFiltreeDebit.stream().collect(Collectors.toList());

							}

							if (product) {
								jrFiltreeDebit = jrFiltreeDebit.stream()
										.filter(x -> x.getProductCategory().equals(jrSM.getProductCategory()))
										.collect(Collectors.toList());
							} else {
								jrFiltreeDebit = jrFiltreeDebit.stream().collect(Collectors.toList());

							}

							if (narrative1) {
								jrFiltreeDebit = jrFiltreeDebit.stream().filter(
										x -> x.getTransactionDescription().equals(jrSM.getTransactionDescription()))
										.collect(Collectors.toList());
							} else {
								jrFiltreeDebit = jrFiltreeDebit.stream().collect(Collectors.toList());

							}

							// supprimer les doublon
							jrFiltreeDebit = jrFiltreeDebit.stream().distinct().collect(Collectors.toList());

							if (!jrFiltreeDebit.isEmpty()) {

								for (JournalEntry j : jrFiltreeDebit) {
									boolean reconStatus = j.getRecStatus();
									if (j.getAmount() != null && (reconStatus == false)) {
										sommeDebit += Math.abs(j.getAmount());

										sommeDebitArrondi = (double) Math.round(sommeDebit * 100000) / 100000;
										// df.format(sommeDebit) ;
										// DecimalFormat df = new DecimalFormat("0.000");
									}
								}

								// filtrer les credit

								if (datebooking) {
									jrFiltreeCredit = journalEntriesBG.stream()
											.filter(x -> x.getBookDate().equals(jrSM.getBookDate()))
											.collect(Collectors.toList());
								} else {
									jrFiltreeCredit = journalEntriesBG.stream().collect(Collectors.toList());

								}

								// if (batchref) {
								// jrFiltreeCredit = jrFiltreeCredit.stream()
								// .filter(x ->
								// x.getDocNumber().equals(jrSM.getDocNumber()))
								// .collect(Collectors.toList());
								// } else {
								// jrFiltreeCredit =
								// jrFiltreeCredit.stream().collect(Collectors.toList());
								//
								// }

								if (batchref) {
									jrFiltreeCredit = jrFiltreeCredit.stream().filter(
											x -> x.getTransactionReference().equals(jrSM.getTransactionReference()))
											.collect(Collectors.toList());
								} else {
									jrFiltreeCredit = jrFiltreeCredit.stream().collect(Collectors.toList());

								}

								if (contractID) {
									jrFiltreeCredit = jrFiltreeCredit.stream()
											.filter(x -> x.getContractId().equals(jrSM.getContractId()))
											.collect(Collectors.toList());
								} else {
									jrFiltreeCredit = jrFiltreeCredit.stream().collect(Collectors.toList());

								}

								if (product) {
									jrFiltreeCredit = jrFiltreeCredit.stream()
											.filter(x -> x.getProductCategory().equals(jrSM.getProductCategory()))
											.collect(Collectors.toList());
								} else {
									jrFiltreeCredit = jrFiltreeCredit.stream().collect(Collectors.toList());

								}

								if (narrative1) {
									jrFiltreeCredit = jrFiltreeCredit.stream().filter(
											x -> x.getTransactionDescription().equals(jrSM.getTransactionDescription()))
											.collect(Collectors.toList());
								} else {
									jrFiltreeCredit = jrFiltreeCredit.stream().collect(Collectors.toList());

								}

								// supprimer les doublon
								jrFiltreeCredit = jrFiltreeCredit.stream().distinct().collect(Collectors.toList());

								if (!jrFiltreeCredit.isEmpty()) {

									for (JournalEntry j : jrFiltreeCredit) {
										boolean reconStatus = j.getRecStatus();
										if (j.getAmount() != null && (reconStatus == false)) {
											sommeCredit += Math.abs(j.getAmount());

											// DecimalFormat df = new DecimalFormat("0.000");
											sommeCreditArrondi = (double) Math.round(sommeCredit * 100000) / 100000;

											// Double.parseDouble(df.format(sommeCredit)) ;

										}
									}

									if (sommeDebitArrondi == sommeCreditArrondi
											&& (sommeDebitArrondi != 0 && sommeCreditArrondi != 0)) {

							String referenceJE = sdf.format(dateLettrage) ;
												
										for (JournalEntry je : jrFiltreeCredit) {

											//je.setReconciliationCode(jrSM.getjECode());
											
											// modification le 29-11-2022
											
											int cpt = 1;
											
											je.setReconciliationCode(referenceJE + "."
											+eventEngineRule.getEventRulesCode()+"."+ String.valueOf(positionNum) + "." + String.valueOf(cpt));
											je.setRecStatus(true);

											listToUpdate.add(je);
											// journalEntryServiceImpl.updateJournalEntry(jrSM);

											ArrayList<String> aList = new ArrayList<String>();

											aList.add(sdf.format(je.getBookDate()));
											// aList.add(je.getDocNumber());
											aList.add(je.getTransactionReference());

											aList.add(je.getContractId());
											aList.add(je.getLineNumber());
											aList.add(je.getTransactionCode());
											aList.add(je.getProductCategory());
											aList.add(je.getAccountCode());
											aList.add(je.getTransactionDescription());
											aList.add(customFormtFloat("#,###.###", je.getAmount(), "", '.', 3,
													cmp.getFunctionCurrency().getCurrencyCode()));

											aList.add(customFormtFloat("#,###.###", je.getForeignCurrencyAmount(), "",
													'.', 3, cmp.getFunctionCurrency().getCurrencyCode()));
											aList.add(je.getCurrency());
											aList.add(je.getjECode());
											aList.add(je.getReconciliationCode());
											aList.add("Multiple Line");
											aList.add("1");

											valueColumns.add(aList);

										} // end for liste credit pour la
											// reconciliation

										for (JournalEntry je : jrFiltreeDebit) {

											//je.setReconciliationCode(jrSM.getjECode());
											
											
											int cpt = 1;
											//jrSM.setReconciliationCode(jrSM.getjECode());
											je.setReconciliationCode(referenceJE + "."
											+eventEngineRule.getEventRulesCode()+"."+ String.valueOf(positionNum) + "." + String.valueOf(cpt));
											//je.setReconciliationCode(jrSM.getReconciliationCode());
											
											je.setRecStatus(true);

											listToUpdate.add(je);
											// journalEntryServiceImpl.updateJournalEntry(jrSM);

											ArrayList<String> aList = new ArrayList<String>();

											aList.add(sdf.format(je.getBookDate()));
											// aList.add(je.getDocNumber());
											aList.add(je.getTransactionReference());

											aList.add(je.getContractId());
											aList.add(je.getLineNumber());
											aList.add(je.getTransactionCode());
											aList.add(je.getProductCategory());
											aList.add(je.getAccountCode());

											aList.add(je.getTransactionDescription());
											aList.add(customFormtFloat("#,###.###", je.getAmount(), "", '.', 3,
													cmp.getFunctionCurrency().getCurrencyCode()));

											aList.add(customFormtFloat("#,###.###", je.getForeignCurrencyAmount(), "",
													'.', 3, cmp.getFunctionCurrency().getCurrencyCode()));
											aList.add(je.getCurrency());
											aList.add(je.getjECode());
											aList.add(je.getReconciliationCode());
											aList.add("Multiple Line");
											aList.add("1");

											valueColumns.add(aList);

										} // end for liste Debit pour la
											// reconciliation
											// journalEntriesSM.removeAll(jrFiltreeDebit);
											// journalEntriesBG.removeAll(jrFiltreeCredit)
											// ;
									} // fin if test egalit� des sommes

								} // fin test if listCredit is not empty

							} // fin if listedebit not empty

						} // fin for (JournalEntry jrSM : journalEntriesSM)

					} // fin ligne multiple

				}
			}
		}
		// }

		try {
			if (!listToUpdate.isEmpty()) {
				journalEntryServiceImpl.updateListJournalEntry(listToUpdate);
				if (eventEngineRule.getEventRuleLines().get(0).getCbrPath() != null) {
					utils.InOutputStreamCSVBalance(
							eventEngineRule.getEventRuleLines().get(0).getCbrPath().concat(reconcType), labelColumns,
							valueColumns, reconcType + " Reconciliation " + "                 Created Date : "
									+ sdf.format(new Date()).toString());
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			// compteurError++;
			e.printStackTrace();
		}

	}

	//////////////////////////////// fin lettrage
	//////////////////////////////// /////////////////////////////////////////////////////////